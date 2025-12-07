package cgodin.qc.ca.petitgazouillis.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cgodin.qc.ca.petitgazouillis.R
import cgodin.qc.ca.petitgazouillis.data.models.UserProfile
import cgodin.qc.ca.petitgazouillis.databinding.ItemUserBinding
import com.bumptech.glide.Glide

class UserListAdapter(
    private val onUserClick: (UserProfile) -> Unit,
    private val onFollowToggle: (UserProfile) -> Unit
) : RecyclerView.Adapter<UserListAdapter.UserVH>() {

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8000"
    }

    private var items: List<UserProfile> = emptyList()

    fun submit(list: List<UserProfile>) {
        items = list
        notifyDataSetChanged()
    }

    inner class UserVH(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserVH(binding)
    }

    override fun onBindViewHolder(holder: UserVH, position: Int) {
        val item = items[position]

        fun String?.asFullUrl(): String? {
            return this?.takeIf { it.isNotBlank() }?.let { url ->
                if (url.startsWith("http")) url else "$BASE_URL${url.trim()}"
            }
        }

        holder.binding.userName.text = item.nom_utilisateur
        val followers = item.followers_count ?: 0
        val following = item.following_count ?: 0
        holder.binding.userStats.text = holder.binding.root.context.getString(
            R.string.user_stats_format,
            followers,
            following
        )

        Glide.with(holder.binding.userAvatar.context)
            .load(item.photo_url.asFullUrl())
            .placeholder(R.drawable.ic_person_placeholder)
            .error(R.drawable.ic_person_placeholder)
            .circleCrop()
            .into(holder.binding.userAvatar)

        val isFollowing = item.is_following == true
        holder.binding.btnFollowToggle.text = holder.binding.root.context.getString(
            if (isFollowing) R.string.unfollow else R.string.follow
        )

        holder.binding.btnFollowToggle.setOnClickListener { onFollowToggle(item) }
        holder.binding.root.setOnClickListener { onUserClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
