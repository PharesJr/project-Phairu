package com.example.project_phairu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.example.project_phairu.Model.SliderModel

class SliderAdapter(
    private var sliderItems:List<SliderModel>,
    private var viewPager2: ViewPager2
): RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {

    private lateinit var context: Context
    private val runnable = Runnable {
        sliderItems = sliderItems
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SliderAdapter.SliderViewHolder {
      context=parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.slider_item_container,parent,false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderAdapter.SliderViewHolder, position: Int) {
     holder.setImage(sliderItems[position],context)
        if(position==sliderItems.lastIndex-1){
            viewPager2.post(runnable)
        }
    }

    override fun getItemCount(): Int = sliderItems.size


    class SliderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView =itemView.findViewById(R.id.imageSlide)
        fun setImage(sliderModel: SliderModel, context: Context){
            val requestOptions = RequestOptions().transform(CenterInside())

           Glide.with(context)
               .load(sliderModel.url)
               .apply(requestOptions)
               .into(imageView)
        }
    }
}