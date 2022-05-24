package com.xotkins.noticeboard.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.accounthelper.AccountHelper
import com.xotkins.noticeboard.adapters.AnnouncementRcAdapter
import com.xotkins.noticeboard.databinding.ActivityMainBinding
import com.xotkins.noticeboard.constants.DialogConst
import com.xotkins.noticeboard.dialoghelper.DialogHelper
import com.xotkins.noticeboard.model.Announcement
import com.xotkins.noticeboard.viewmodel.FirebaseViewModel


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AnnouncementRcAdapter.Listener{
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    private lateinit var tvAccountEmail: TextView
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    val adapter = AnnouncementRcAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()


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

    override fun onStart() { //обновление почты
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    private fun initViewModel(){ //обновление через мввм
        firebaseViewModel.liveAnnouncementsData.observe(this){
            adapter.updateAdapter(it)
            binding.mainContent.tvEmpty.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun init(){ //надуваем разметку экрана
        setSupportActionBar(binding.mainContent.toolbar)
        var toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.mainContent.toolbar,
            R.string.open,
            R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccountEmail = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
    }

    private fun initRecyclerView(){ //запуск rcView
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { //выдвижное меню
        when(item.itemId){
            R.id.id_my_ads ->{
                Toast.makeText(this, "Pressed id_my_adc", Toast.LENGTH_LONG).show()
            }
            R.id.id_car ->{
                Toast.makeText(this, "Pressed id_car", Toast.LENGTH_LONG).show()
            }
            R.id.id_pc ->{
                Toast.makeText(this, "Pressed id_pc", Toast.LENGTH_LONG).show()
            }
            R.id.id_smartphone ->{
                Toast.makeText(this, "Pressed id_smartphone", Toast.LENGTH_LONG).show()
            }
            R.id.id_dm ->{
                Toast.makeText(this, "Pressed id_dm", Toast.LENGTH_LONG).show()
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
    
    private fun setBottomNavListener(){ //нижнее меню
        binding.mainContent.bNavView.setOnItemSelectedListener{item ->
            when(item.itemId){
                R.id.id_home->{
                    firebaseViewModel.loadAllAnnouncements()
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
   //     resources.getString(R.string.note_reg)

    fun uiUpdate(user: FirebaseUser?){ //обновление почты после входа/выхода
        if(user == null){
            dialogHelper.accountHelper.signInAnonymously(object: AccountHelper.Listener{
                override fun onComplete() {
                    tvAccountEmail.text = getString(R.string.sign_in_guest)
                }
            })
        }else if(user.isAnonymous){ //если заходим как гость показываем гость
            tvAccountEmail.text = getString(R.string.sign_in_guest)
        }else if(!user.isAnonymous){//если вошли под почтой показываем почту
            tvAccountEmail.text = user.email
        }
    }

    companion object{
        const val EDIT_STATE = "edit_state"
        const val ANNOUNCEMENTS_DATA = "announcements_data"
    }

    override fun onDeleteItem(announcement: Announcement) {//запуск ф-ции на главном активити с помощью интерфейса(с МВВМ)
        firebaseViewModel.deleteAnnouncement(announcement)
    }

    override fun onAnnouncementViewed(announcement: Announcement) {//запуск ф-ции на главном активити с помощью интерфейса(с МВВМ)
        firebaseViewModel.announcementViewed(announcement)
    }

    override fun onFavClicked(announcement: Announcement) {//запуск ф-ции на главном активити с помощью интерфейса(с МВВМ)
        firebaseViewModel.onFavClick(announcement)
    }
}