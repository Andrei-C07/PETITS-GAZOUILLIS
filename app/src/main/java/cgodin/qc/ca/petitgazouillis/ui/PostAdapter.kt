package cgodin.qc.ca.petitgazouillis.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import cgodin.qc.ca.petitgazouillis.data.models.Publication
import cgodin.qc.ca.petitgazouillis.databinding.ItemPostBinding

data class PostUI(
    val username: String,
    val text: String,
    val photoUrl: String?,
    val userId: Int,
)

class PostAdapter(
    private val onItemClick: (PostUI) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostVH>() {

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8000"
    }

    private var list = listOf<PostUI>()

    fun submitList(newList: List<PostUI>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class PostVH(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostVH {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostVH(binding)
    }

    override fun onBindViewHolder(holder: PostVH, position: Int) {
        val item = list[position]
        holder.binding.username.text = item.username
        holder.binding.postText.text = item.text
        val photoUrl = item.photoUrl?.takeIf { it.isNotBlank() }?.let { url ->
            if (url.startsWith("http")) url else "$BASE_URL${url.trim()}"
        }
        Glide.with(holder.binding.avatar.context)
            .load(photoUrl)
            .placeholder(cgodin.qc.ca.petitgazouillis.R.drawable.ic_person_placeholder)
            .error(cgodin.qc.ca.petitgazouillis.R.drawable.ic_person_placeholder)
            .circleCrop()
            .into(holder.binding.avatar)
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = list.size
}
