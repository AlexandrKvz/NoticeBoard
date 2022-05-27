package com.xotkins.noticeboard.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.accounthelper.AccountHelper
import com.xotkins.noticeboard.adapters.AnnouncementRcAdapter
import com.xotkins.noticeboard.constants.ConstIntentMainActivity
import com.xotkins.noticeboard.databinding.ActivityMainBinding
import com.xotkins.noticeboard.constants.DialogConst
import com.xotkins.noticeboard.dialoghelper.DialogHelper
import com.xotkins.noticeboard.model.Announcement
import com.xotkins.noticeboard.utils.FilterManager
import com.xotkins.noticeboard.viewmodel.FirebaseViewModel


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AnnouncementRcAdapter.Listener{
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    private lateinit var tvAccountEmail: TextView
    private lateinit var imAccount: ImageView
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    lateinit var filterLauncher: ActivityResultLauncher<Intent>
    val adapter = AnnouncementRcAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null
    private var filter: String = "empty"
    private var filterDatabase: String = "" //фильтр нужен для базы данных


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initAds()
        init()
        onActivityResult()
        initRecyclerView()
        initViewModel()
        setBottomNavListener()
        scrollListener()
        onActivityResultFilter()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {// нажимаем на кнопку фильтр и передаём данные через лаунчерФильтр
        if(item.itemId == R.id.id_filter) {
            val intent = Intent(this@MainActivity, FilterActivity::class.java).apply {
                putExtra(FilterActivity.FILTER_KEY, filter)
            }
            filterLauncher.launch(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
        binding.mainContent.adView2.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.mainContent.adView2.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mainContent.adView2.destroy()
    }

    private fun initAds(){
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.mainContent.adView2.loadAd(adRequest)
    }

    private fun onActivityResult() { //лаунчер для входа по гуглу
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account != null){
                    Log.d("My Log", "Api error 0")
                    dialogHelper.accountHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            }catch (e:ApiException){
                Log.d("My Log", "Api error : ${e.message}")
                }
            }
        }

    private fun onActivityResultFilter(){ //лаунчер для фильтра
        filterLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == RESULT_OK){
                filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!! //здесь сохраняется состояние, что мы выбрали в фильтре и возвращать его назад
               // Log.d("MyLog", "filter: $filter")
               // Log.d("MyLog", "getFilter: ${FilterManager.getFilter(filter)}")
                filterDatabase = FilterManager.getFilter(filter)
            }else if(it.resultCode == RESULT_CANCELED){
                filterDatabase = ""
                filter = "empty"
            }
        }
    }

    override fun onStart() { //обновление почты
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    private fun initViewModel(){ //обновление через мввм
        firebaseViewModel.liveAnnouncementsData.observe(this){
            val list = getAnsByCategory(it)
            if(!clearUpdate) {
                adapter.updateAdapter(list) //тут обновляет список прибавляя новые объявления
            }else{
                adapter.updateAdapterWithClear(list)
            }
            binding.mainContent.tvEmpty.visibility = if(adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun getAnsByCategory(list: ArrayList<Announcement>): ArrayList<Announcement>{ //тут берём объявления по категориям
        val tempList = ArrayList<Announcement>() //создаём список
        tempList.addAll(list) //загружаем во временный список оригинальный список
        if(currentCategory != getString(R.string.def)){
            tempList.clear()
            list.forEach {
                if(currentCategory == it.category)tempList.add(it) //здесь при скролле в категории не будут добавляться объявления из других категорий
            }
        }
        tempList.reverse()
        return tempList
    }

    private fun init(){ //надуваем разметку экрана
        currentCategory = getString(R.string.def)
        navViewSettings()
        setSupportActionBar(binding.mainContent.toolbar)
        var toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.mainContent.toolbar,
            R.string.open,
            R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccountEmail = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccountImage)
    }

    private fun initRecyclerView(){ //запуск rcView
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { //выдвижное меню
        clearUpdate = true//смотреть в ф-цию initViewModel()
        when(item.itemId){
            R.id.id_my_ads ->{
                Toast.makeText(this, "Pressed id_my_adc", Toast.LENGTH_LONG).show()
            }
            R.id.id_car ->{
                getAnsFromCategory(getString(R.string.ad_car))
            }
            R.id.id_pc ->{
                getAnsFromCategory(getString(R.string.ad_pc))
            }
            R.id.id_smartphone ->{
                getAnsFromCategory(getString(R.string.ad_smartphone))
            }
            R.id.id_dm ->{
                getAnsFromCategory(getString(R.string.ad_dm))
            }
            R.id.id_sign_up ->{
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }
            R.id.id_sign_in ->{
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }
            R.id.id_sign_out ->{
                if(mAuth.currentUser?.isAnonymous == true){
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                        return true
                }
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accountHelper.signOutGoogle()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAnsFromCategory(category: String){ //ф-ция для фильтрации объявлений по категории
        currentCategory = category
        firebaseViewModel.loadAllAnnouncementsFromCategoryFirstPage(category, filterDatabase)
    }
    
    private fun setBottomNavListener(){ //нижнее меню
        binding.mainContent.bNavView.setOnItemSelectedListener{item ->
            clearUpdate = true//смотреть в ф-цию initViewModel()
            when(item.itemId){
                R.id.id_home->{
                    currentCategory = getString(R.string.def)
                    firebaseViewModel.loadAllAnnouncementsFirstPage(filterDatabase)
                    binding.mainContent.toolbar.title = getString(R.string.def)
                }
                R.id.id_favs->{
                    firebaseViewModel.loadMyFavs()
                    binding.mainContent.toolbar.title = getString(R.string.ad_my_favs)
                }
                R.id.id_my_ads->{
                    firebaseViewModel.loadMyAnnouncements()
                    binding.mainContent.toolbar.title = getString(R.string.ad_my_ads)
                }
                R.id.id_new_ads->{
                    val intent = Intent(this, EditAnnouncementsActivity::class.java)
                    startActivity(intent)

                }
            }
            true
        }
    }
   // tvAccountEmail.text = if(user == null){
   // resources.getString(R.string.note_reg)

    fun uiUpdate(user: FirebaseUser?){ //обновление почты после входа/выхода
        if(user == null){
            dialogHelper.accountHelper.signInAnonymously(object: AccountHelper.Listener{
                override fun onComplete() {
                    tvAccountEmail.text = getString(R.string.sign_in_guest)
                    imAccount.setImageResource(R.drawable.ic_account_def)//показываем стандартную картинку анонима
                }
            })
        }else if(user.isAnonymous){ //если заходим как гость показываем гость
            tvAccountEmail.text = getString(R.string.sign_in_guest)
            imAccount.setImageResource(R.drawable.ic_account_def) //показываем стандартную картинку анонима
        }else if(!user.isAnonymous){//если вошли под почтой показываем почту
            tvAccountEmail.text = user.email
            Picasso.get().load(user.photoUrl).into(imAccount)//показываем картинку от аккаунта гугла
        }
    }

    override fun onDeleteItem(announcement: Announcement) {//запуск ф-ции на главном активити с помощью интерфейса(с МВВМ)
        firebaseViewModel.deleteAnnouncement(announcement)
    }

    override fun onAnnouncementViewed(announcement: Announcement) {//запуск ф-ции на главном активити с помощью интерфейса(с МВВМ)
        firebaseViewModel.announcementViewed(announcement)
        val intent = Intent(this, DescriptionActivity::class.java) //показываем какое активити хотим открыть
        intent.putExtra(ConstIntentMainActivity.INTENT_MAIN_ANNOUNCEMENT, announcement)//передаём данные под ключевым словом и какой класс передаём
        startActivity(intent) // запускаем
    }

    override fun onFavClicked(announcement: Announcement) {//запуск ф-ции на главном активити с помощью интерфейса(с МВВМ)
        firebaseViewModel.onFavClick(announcement)
    }

    private fun navViewSettings() = with(binding){//ф-ция для изменения цвета категорий
        val menu = navView.menu // находим меню
        val announcementCat = menu.findItem(R.id.announcement_cat) //находим категорию
        val spanAnnouncementCat = SpannableString(announcementCat.title) //получаем текст категории
        spanAnnouncementCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.color_red)), 0, announcementCat.title.length, 0) //тут мы красим текст нужным нам цветом
        announcementCat.title = spanAnnouncementCat //передаём изменённый цвет текста

        val accountCat = menu.findItem(R.id.acc_cat)
        val spanAccountCat = SpannableString(accountCat.title)
        spanAccountCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.color_red)), 0, accountCat.title.length, 0)
        accountCat.title = spanAccountCat
    }

    private  fun scrollListener() = with(binding.mainContent){ //ф-ция для постепенного подгружения объявлений, когда опускаемся вниз
        rcView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(!recyclerView.canScrollVertically(SCROLL_DOWN) && newState == RecyclerView.SCROLL_STATE_IDLE){
                    clearUpdate = false //смотреть в ф-цию initViewModel()
                    val announcementsList = firebaseViewModel.liveAnnouncementsData.value!!
                    if(announcementsList.isNotEmpty()){ //если список не пустой
                        getAnnouncementsFromCategory(announcementsList)
                    }
                }
            }
        })
    }

    private fun getAnnouncementsFromCategory(announcementList: ArrayList<Announcement>) { //ф-ция скролла для любой категории
        announcementList[0].let {
            if (currentCategory == getString(R.string.def)) {
                firebaseViewModel.loadAllAnnouncementsNextPage(it.time, filterDatabase)
            } else {
                firebaseViewModel.loadAllAnnouncementsFromCategoryNextPage(it.category!!, it.time, filterDatabase)
            }
        }
    }

    companion object{
        const val EDIT_STATE = "edit_state"
        const val ANNOUNCEMENTS_DATA = "announcements_data"
        const val SCROLL_DOWN = 1 //  не может подыматься вверх
    }
}