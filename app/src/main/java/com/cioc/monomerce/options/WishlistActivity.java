package com.cioc.monomerce.options;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cioc.monomerce.BackendServer;
import com.cioc.monomerce.R;
import com.cioc.monomerce.entites.Cart;
import com.cioc.monomerce.entites.ListingParent;
import com.cioc.monomerce.product.ItemDetailsActivity;
import com.cioc.monomerce.startup.MainActivity;
import com.cioc.monomerce.utility.ImageUrlUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

import static com.cioc.monomerce.fragments.ImageListFragment.STRING_IMAGE_POSITION;
import static com.cioc.monomerce.fragments.ImageListFragment.STRING_IMAGE_URI;


public class WishlistActivity extends AppCompatActivity {
    private static Context mContext;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    public AsyncHttpClient client;
    public static ArrayList<Cart> wishList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
        mContext = WishlistActivity.this;
        client = new AsyncHttpClient();
        wishList = new ArrayList<>();

        getWishListItem();

//        ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
//        ArrayList<String> wishlistImageUri = imageUrlUtils.getWishlistImageUri();

        recyclerView = findViewById(R.id.recyclerview);
        progressBar =  findViewById(R.id.progressBar);
        final RecyclerView.LayoutManager recyclerViewLayoutManager = new LinearLayoutManager(mContext);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                recyclerView.setLayoutManager(recyclerViewLayoutManager);
                recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(wishList));
            }
        },1000);
    }

    public void getWishListItem() {
        client.get(BackendServer.url+"/api/ecommerce/cart/?&Name__contains=&user=1&typ=favourite",
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object = response.getJSONObject(i);
                                Cart cart = new Cart(object);
                                wishList.add(cart);
                            }
                        } catch (JSONException e) {
                                e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.e("Error "+statusCode, "onFailure2");
                    }
                });
    }



    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<WishlistActivity.SimpleStringRecyclerViewAdapter.ViewHolder> {

        private ArrayList<Cart> mWishlist;
        AsyncHttpClient client;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final SimpleDraweeView mImageView;
            public final LinearLayout mLayoutItem, mLayoutRemove;
            TextView productName, itemPrice, actualPrice, discountPercentage;
            Button moveToCart;
//            AsyncHttpClient client = new AsyncHttpClient();


            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = view.findViewById(R.id.image_wishlist);
                mLayoutItem = view.findViewById(R.id.layout_item_desc);
                mLayoutRemove = view.findViewById(R.id.layout_action1_remove);
//                mImageViewWishlist = (ImageView) view.findViewById(R.id.ic_wishlist);
                productName =  view.findViewById(R.id.product_name);
                itemPrice =  view.findViewById(R.id.item_price);
                actualPrice =  view.findViewById(R.id.actual_price);
                actualPrice.setPaintFlags(actualPrice.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
                discountPercentage =  view.findViewById(R.id.discount_percentage);
                moveToCart =  view.findViewById(R.id.move_cart_button);
            }
        }

        public SimpleStringRecyclerViewAdapter(ArrayList<Cart> wishlist) {
            mWishlist = wishlist;
        }

        @Override
        public WishlistActivity.SimpleStringRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_wishlist_item, parent, false);
            client = new AsyncHttpClient();
            return new WishlistActivity.SimpleStringRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            if (holder.mImageView.getController() != null) {
                holder.mImageView.getController().onDetach();
            }
            if (holder.mImageView.getTopLevelDrawable() != null) {
                holder.mImageView.getTopLevelDrawable().setCallback(null);
//                ((BitmapDrawable) holder.mImageView.getTopLevelDrawable()).getBitmap().recycle();
            }
        }

        @Override
        public void onBindViewHolder(final WishlistActivity.SimpleStringRecyclerViewAdapter.ViewHolder holder, final int position) {
            final Cart cart = mWishlist.get(position);
            final ListingParent parent = cart.getParents().get(0);
            final Uri uri = Uri.parse(cart.getListingParent().getFilesAttachment());
            holder.mImageView.setImageURI(uri);

            holder.productName.setText(parent.getProductName());
            if (parent.getProductDiscount().equals("0")){
                holder.itemPrice.setText("Rs "+parent.getProductPrice());
                holder.actualPrice.setVisibility(View.GONE);
                holder.discountPercentage.setVisibility(View.GONE);
//                mPrice = mPrice + (parent.getProductIntPrice()*Integer.parseInt(cart.getQuantity()));
            } else {
                holder.itemPrice.setText("Rs "+parent.getProductDiscountedPrice());
//                mPrice = mPrice + (parent.getProductIntDiscountedPrice()*Integer.parseInt(cart.getQuantity()));
                holder.discountPercentage.setVisibility(View.VISIBLE);
                holder.discountPercentage.setText("("+parent.getProductDiscount()+"% OFF)");
                holder.actualPrice.setVisibility(View.VISIBLE);
                holder.actualPrice.setText(parent.getProductPrice());
                holder.actualPrice.setPaintFlags(holder.actualPrice.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            }


            holder.mLayoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ItemDetailsActivity.class);
                    intent.putExtra(STRING_IMAGE_URI, cart.getListingParent().getFilesAttachment());
                    intent.putExtra(STRING_IMAGE_POSITION, position);
                    intent.putExtra("listingLitePk", parent.getPk());
                    mContext.startActivity(intent);
                }
            });

            //Set click action for wishlist
            holder.moveToCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveCart(cart);
                    MainActivity.notificationCountCart++;

//                    ImageUrlUtils imageUrlUtils = new ImageUrlUtils();
//                    imageUrlUtils.removeWishlistImageUri(position);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mWishlist.size();
        }

        public void moveCart(Cart cart) {
            RequestParams params = new RequestParams();
            params.put("typ", "cart");
            client.patch(BackendServer.url+"/api/ecommerce/cart/"+ cart.getPk()+"/", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Toast.makeText(mContext, "onSuccess", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(mContext, "onFailure", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
