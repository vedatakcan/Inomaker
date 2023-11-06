package com.vedatakcan.inomaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.helper.widget.MotionPlaceholder
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.vedatakcan.inomaker.databinding.ItemViewBinding

class CategoriesAdapter(

): RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {



    private var categoriesList: MutableList<CategoriesModel> = mutableListOf()
    private lateinit var navController: NavController




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemViewBinding.inflate(inflater, parent, false)

        navController = Navigation.findNavController(parent)
        return CategoriesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        holder.bind(categoriesList[position])
    }


    inner class CategoriesViewHolder(private val binding: ItemViewBinding):
         RecyclerView.ViewHolder(binding.root){
        fun bind(categoriesModel: CategoriesModel){
            binding.apply {
                tvCategoryName.text = categoriesModel.categoryName
                ivCategoryImage.imageAlpha = categoriesModel.categoryImage
            }
            itemView.setOnClickListener{
                val categoryId = categoriesModel.categoryId
                val bundle = bundleOf("categoryId" to categoryId)
                navController.navigate(R.id.action_optionsFragment_to_imageFragment, bundle)
            }
        }
    }

    fun submitData(list: ArrayList<CategoriesModel>){
        this.categoriesList.clear()
        this.categoriesList.addAll(list)
        notifyDataSetChanged()
    }


}




