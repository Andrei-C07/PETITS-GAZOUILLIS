package cgodin.qc.ca.petitgazouillis.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cgodin.qc.ca.petitgazouillis.databinding.ItemPostBinding

data class PostUI(
    val username: String,
    val text: String,
)

class PostAdapter : RecyclerView.Adapter<PostAdapter.PostVH>() {

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
    }

    override fun getItemCount(): Int = list.size
}