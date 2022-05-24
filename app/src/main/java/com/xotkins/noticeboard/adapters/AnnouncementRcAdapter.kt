package com.xotkins.noticeboard.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.xotkins.noticeboard.R
import com.xotkins.noticeboard.activity.EditAnnouncementsActivity
import com.xotkins.noticeboard.activity.MainActivity
import com.xotkins.noticeboard.model.Announcement
import com.xotkins.noticeboard.databinding.AnnouncementListItemBinding

class AnnouncementRcAdapter(val activity: MainActivity) : RecyclerView.Adapter<AnnouncementRcAdapter.AnnouncementHolder>() {
        val announcementArray = ArrayList<Announcement>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementHolder {
        val binding = AnnouncementListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnouncementHolder(binding, activity)
    }

    override fun onBindViewHolder(holder: AnnouncementHolder, position: Int) {
        holder.setData(announcementArray[position])
    }

    override fun getItemCount(): Int {
        return announcementArray.size
    }

    fun updateAdapter(newList: List<Announcement>){ //ф-ция для заполнения и обновления
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(announcementArray, newList))//обновление списка с помощью DiffUtilHelper
        diffResult.dispatchUpdatesTo(this) //указывается где обновляется
        announcementArray.clear()//сначало очищаем список
        announcementArray.addAll(newList)//добавляем новый
    }

    class AnnouncementHolder(val binding: AnnouncementListItemBinding, val activity: MainActivity): RecyclerView.ViewHolder(binding.root) {
        fun setData(announcement: Announcement) = with(binding){
            tvTitle.text = announcement.title
            tvDescription.text = announcement.description
            tvPrice.text = announcement.price
            tvViewCounter.text = announcement.viewsCounter
            tvFavCounter.text = announcement.favCounter
            if(announcement.isFav){
                ibFav.setImageResource(R.drawable.ic_fav)
            }else{
                ibFav.setImageResource(R.drawable.ic_fav_normal)
            }
            showEditPanel(isOwner(announcement))
            ibFav.setOnClickListener{//слушатель нажатия на избранное элемент объявления
               if(activity.mAuth.currentUser?.isAnonymous == false) activity.onFavClicked(announcement)
            }
            itemView.setOnClickListener{//слушатель нажатия на весь элемент объявления
                activity.onAnnouncementViewed(announcement)
            }
            imEditAnnouncement.setOnClickListener(onClickEdit(announcement)) //запускаем ф-цию onClickEdit
            imDelete.setOnClickListener{//ф-ция запускаем интерфейс для удаления
                activity.onDeleteItem(announcement)
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