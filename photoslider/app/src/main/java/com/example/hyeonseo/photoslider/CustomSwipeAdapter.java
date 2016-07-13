package com.example.hyeonseo.photoslider;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by HyeonSeo on 7/13/2016.
 */
public class CustomSwipeAdapter extends PagerAdapter{
    private int[] image_resources=  {R.drawable.goghlowres,R.drawable.puglowres,R.drawable.whateverthisif};

    private Context ctx;
    private LayoutInflater layoutInflater;

    public CustomSwipeAdapter (Context ctx)
    {
        this.ctx= ctx;
    }
    @Override
    public int getCount() {
        return image_resources.length;

    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return (view == (LinearLayout)o);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater= (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view= layoutInflater.inflate(R.layout.swipe_layout,container,false);
        ImageView imageView=    (ImageView)item_view.findViewById(R.id.imageview);
        TextView  textView= (TextView)item_view.findViewById(R.id.imagecount);
        imageView.setImageResource(image_resources[position]);
        textView.setText("image :"+position);
        container.addView(item_view);
        return item_view ;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);
    }
}
