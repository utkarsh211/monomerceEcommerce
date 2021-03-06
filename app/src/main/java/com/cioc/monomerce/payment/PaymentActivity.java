package com.cioc.monomerce.payment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cioc.monomerce.R;
import com.cioc.monomerce.entites.Cart;
import com.cioc.monomerce.options.CartListActivity;
import com.cioc.monomerce.product.ItemDetailsActivity;
import com.cioc.monomerce.utility.ImageUrlUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.githang.stepview.StepView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.cioc.monomerce.fragments.ImageListFragment.STRING_IMAGE_POSITION;
import static com.cioc.monomerce.fragments.ImageListFragment.STRING_IMAGE_URI;


public class PaymentActivity extends AppCompatActivity {
    private static Context mContext;
    TextView selectedAddress;
    TextView textAmount, paymentBtn;
    RecyclerView recyclerView;
    ArrayList<String> addresses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        String address = getIntent().getExtras().getString("address");

        mContext = PaymentActivity.this;
        init();
//        ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
//        ArrayList<String> cartlistImageUri = imageUrlUtils.getCartListImageUri();
        //Show cart layout based on items
//        setCartLayout();
        StepView mStepView = (StepView) findViewById(R.id.step_view);
        List<String> steps = Arrays.asList(new String[]{"Selected Items", "Shipping Address", "Review Your Order"});
        mStepView.setSteps(steps);
        mStepView.selectedStep(3);

        RecyclerView.LayoutManager recylerViewLayoutManager = new LinearLayoutManager(mContext);

        recyclerView.setLayoutManager(recylerViewLayoutManager);
        recyclerView.setAdapter(new PaymentActivity.PaymentRecyclerViewAdapter(recyclerView, CartListActivity.cartList));

        selectedAddress.setText(address);
        textAmount.setText(getIntent().getStringExtra("totalPrice"));

    }

    public void init(){
        selectedAddress = findViewById(R.id.selected_address);
        paymentBtn = findViewById(R.id.payment_text_button);
        textAmount = findViewById(R.id.text_amount);
        recyclerView = findViewById(R.id.recyclerview_payment);
    }

    public static class PaymentRecyclerViewAdapter
            extends RecyclerView.Adapter<PaymentActivity.PaymentRecyclerViewAdapter.ViewHolder> {

        private ArrayList<Cart> mPaymentlist;
        private RecyclerView mRecyclerView;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final SimpleDraweeView mImageView;
            public final LinearLayout mLayoutItem;
            TextView actualPrice, discountPercentage;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (SimpleDraweeView) view.findViewById(R.id.image_cartlist);
                mLayoutItem = (LinearLayout) view.findViewById(R.id.layout_item_desc);
                actualPrice =  view.findViewById(R.id.actual_price);
                actualPrice.setPaintFlags(actualPrice.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
                discountPercentage =  view.findViewById(R.id.discount_percentage);
            }
        }

        public PaymentRecyclerViewAdapter(RecyclerView recyclerView, ArrayList<Cart> paymentlist) {
            mPaymentlist = paymentlist;
            mRecyclerView = recyclerView;
        }

        @Override
        public PaymentActivity.PaymentRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_cartlist_payment, parent, false);
            return new PaymentActivity.PaymentRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onViewRecycled(PaymentActivity.PaymentRecyclerViewAdapter.ViewHolder holder) {
            if (holder.mImageView.getController() != null) {
                holder.mImageView.getController().onDetach();
            }
            if (holder.mImageView.getTopLevelDrawable() != null) {
                holder.mImageView.getTopLevelDrawable().setCallback(null);
//                ((BitmapDrawable) holder.mImageView.getTopLevelDrawable()).getBitmap().recycle();
            }
        }

        @Override
        public void onBindViewHolder(final PaymentActivity.PaymentRecyclerViewAdapter.ViewHolder holder, final int position) {
            final Cart cart = mPaymentlist.get(position);

            final Uri uri = Uri.parse(cart.getListingParent().getFilesAttachment());
            holder.mImageView.setImageURI(uri);
            holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ItemDetailsActivity.class);
                    intent.putExtra(STRING_IMAGE_URI, cart.getListingParent().getFilesAttachment());
                    intent.putExtra(STRING_IMAGE_POSITION, position);
                    mContext.startActivity(intent);
                }
            });

//            //Set click action
//            holder.mLayoutRemove.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
//                    imageUrlUtils.removeCartListImageUri(position);
//                    notifyDataSetChanged();
//                    //Decrease notification count
//                    MainActivity.notificationCountCart--;
//
//                }
//            });
//
//            //Set click action
//            holder.mLayoutEdit.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return mPaymentlist.size();
        }
    }



}
