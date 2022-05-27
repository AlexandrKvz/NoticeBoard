package com.xotkins.noticeboard.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.activity.DescriptionActivity
import com.xotkins.noticeboard.activity.EditAnnouncementsActivity
import com.xotkins.noticeboard.activity.MainActivity
import com.xotkins.noticeboard.constants.ConstIntentMainActivity
import com.xotkins.noticeboard.model.Announcement
import com.xotkins.noticeboard.databinding.AnnouncementListItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AnnouncementRcAdapter(val activity: MainActivity) : RecyclerView.Adapter<AnnouncementRcAdapter.AnnouncementHolder>() {
    val announcementArray = ArrayList<Announcement>()
    private var timeFormatter: SimpleDateFormat? = null

    init{
        timeFormatter = SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.getDefault()) //здесь указываем формат нашего времени и дату для объявления
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementHolder {
        val binding = AnnouncementListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnouncementHolder(binding, activity, timeFormatter!!)
    }

    override fun onBindViewHolder(holder: AnnouncementHolder, position: Int) {
        holder.setData(announcementArray[position])
    }

    override fun getItemCount(): Int {
        return announcementArray.size
    }

    fun updateAdapter(newList: List<Announcement>){ //ф-ция для заполнения и обновления
        val tempArray = ArrayList<Announcement>() //переменая где весь список
        tempArray.addAll(announcementArray) //старый список до обновления после скрола
        tempArray.addAll(newList)//новый список после обновления скролла

        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(announcementArray, tempArray))//обновление списка с помощью DiffUtilHelper, он понимает что есть старый список и добавляет к нему ещё новый
        diffResult.dispatchUpdatesTo(this) //указывается где обновляется
        announcementArray.clear()//сначало очищаем список
        announcementArray.addAll(tempArray)//добавляем новый
    }

    fun updateAdapterWithClear(newList: List<Announcement>){ //ф-ция для заполнения и обновления
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(announcementArray, newList))//обновление списка с помощью DiffUtilHelper
        diffResult.dispatchUpdatesTo(this) //указывается где обновляется
        announcementArray.clear()//сначало очищаем список
        announcementArray.addAll(newList)//добавляем новый
    }

    class AnnouncementHolder(val binding: AnnouncementListItemBinding, val activity: MainActivity, val formatter: SimpleDateFormat): RecyclerView.ViewHolder(binding.root) {

        fun setData(announcement: Announcement) = with(binding){
            tvTitle.text = announcement.title
            tvDescription.text = announcement.description
            tvPrice.text = announcement.price
            tvViewCounter.text = announcement.viewsCounter
            tvFavCounter.text = announcement.favCounter

            val publishTime = getTimeFromMillis(announcement.time)
            tvPublishTime.text = publishTime

            Picasso.get().load(announcement.image1).into(mainImage) //здесь мы показываем первую картинку из объявления

            isFav(announcement)
            showEditPanel(isOwner(announcement))
            mainOnClick(announcement)
        }

        private fun getTimeFromMillis(timeMillis: String): String{ // ф-ция для превращения в реально время из миллисекунд
            val c = Calendar.getInstance()
            c.timeInMillis = timeMillis.toLong()
            return formatter.format(c.time)
        }

        private fun mainOnClick(announcement: Announcement) = with(binding){
            ibFav.setOnClickListener{//слушатель нажатия на избранное элемент объявления
                if(activity.mAuth.currentUser?.isAnonymous == false) activity.onFavClicked(announcement)
            }
            itemView.setOnClickListener{//слушатель нажатия на весь элемент объявления
                activity.onAnnouncementViewed(announcement)//показываем сколько просмотров
            }
            imEditAnnouncement.setOnClickListener(onClickEdit(announcement)) //запускаем ф-цию onClickEdit
            imDelete.setOnClickListener{//ф-ция запускаем интерфейс для удаления
                activity.onDeleteItem(announcement)
            }
        }

        private fun isFav(announcement: Announcement){
            if(announcement.isFav){
                binding.ibFav.setImageResource(R.drawable.ic_fav)
            }else{
                binding.ibFav.setImageResource(R.drawable.ic_fav_normal)
            }
        }

        private fun onClickEdit(announcement: Announcement): View.OnClickListener{ //создаём слушатель нажатия для редактирования объявления
            return View.OnClickListener {
                val editIntent = Intent(activity, EditAnnouncementsActivity::class.java).apply {
                    putExtra(MainActivity.EDIT_STATE, true)
                    putExtra(MainActivity.ANNOUNCEMENTS_DATA, announcement)
                }
                activity.startActivity(editIntent)
            }
        }

        private fun isOwner(announcement: Announcement): Boolean{ //ф-ция для проверки кто видит объявление, владелец или другой пользователь
            return announcement.uid == activity.mAuth.uid //если индетификатор пользователя равен индетификатору объявленрия, то true
        }

        private fun showEditPanel(isOwner: Boolean){//ф-ция для отображение EditPanel
            if(isOwner){
                binding.editPanel.visibility = View.VISIBLE
            }else{
                binding.editPanel.visibility = View.GONE
            }
        }
    }

    interface  Listener{
        fun onDeleteItem(announcement: Announcement)
        fun onAnnouncementViewed(announcement: Announcement)
        fun onFavClicked(announcement: Announcement)
    }
}