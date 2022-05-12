package com.potensiutama.kusenstoreadmin.ui.data_penjualan;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.potensiutama.kusenstoreadmin.EventBus.ChangeMenuClick;
import com.potensiutama.kusenstoreadmin.EventBus.LoadOrderEvent;
import com.potensiutama.kusenstoreadmin.R;
import com.potensiutama.kusenstoreadmin.adapter.MyDataPenjualanAdapter;
import com.potensiutama.kusenstoreadmin.common.Common;
import com.potensiutama.kusenstoreadmin.model.OrderModel;
import com.potensiutama.kusenstoreadmin.remote.IFCMService;
import com.potensiutama.kusenstoreadmin.remote.RetrofitFCMClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;

public class DataPenjualanFragment extends Fragment{

    String bulan,hari;
    private int mYear, mMonth, mDay;
    private String tanggal;
    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;
    @BindView(R.id.txt_filter_tanggal)
    TextView txt_filter_tanggal;
    @BindView(R.id.txt_total_data_penjualan)
    TextView txt_total_data_penjualan;
    @BindView(R.id.txt_jumlah_item_penjualan)
    TextView txt_jumlah_item_penjualan;

    List<OrderModel> dataPenjualanModelList;

    Unbinder unbinder;

    LayoutAnimationController layoutAnimationController;
    MyDataPenjualanAdapter adapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;

    private DataPenjualanViewModel dataPenjualanViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dataPenjualanViewModel =
                ViewModelProviders.of(this).get(DataPenjualanViewModel.class);
        View root = inflater.inflate(R.layout.fragment_data_penjualan, container, false);
        unbinder = ButterKnife.bind(this,root);
        initViews();
        dataPenjualanViewModel.getMessageError().observe(getViewLifecycleOwner(),s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });
        dataPenjualanViewModel.getDataPenjualanModelMutableLiveData().observe(getViewLifecycleOwner(),dataPenjualanModels -> {
            if(dataPenjualanModels != null){
                dataPenjualanModelList = dataPenjualanModels;
                adapter = new MyDataPenjualanAdapter(getContext(),dataPenjualanModelList);
                recycler_order.setAdapter(adapter);
                recycler_order.setLayoutAnimation(layoutAnimationController);

                txt_jumlah_item_penjualan.setText(new StringBuilder("(")
                        .append(adapter.getItemCount())
                        .append(")"));

                txt_total_data_penjualan.setText(new StringBuilder("Rp")
                        .append(Common.formatPrice(Common.totalDataPenjualan)));
            }
        });
        return root;
    }

    private void initViews() {

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        setHasOptionsMenu(true);

        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        txt_filter_tanggal.setText(new StringBuilder("Tanggal : ")
                .append("Semua Tanggal"));
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.data_penjualan_tanggal_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_pilih_tanggal_penjualan){
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            if(monthOfYear < 10){
                                bulan = "0" + (monthOfYear+1);
                            }else{
                                bulan = ""+ (monthOfYear+1);
                            }
                            if(dayOfMonth < 10){
                                hari  = "0" + dayOfMonth;
                            }else{
                                hari = ""+dayOfMonth;
                            }

                            tanggal = hari + "/" + bulan + "/" + year;

                            txt_filter_tanggal.setText(new StringBuilder("Tanggal : ")
                                    .append(tanggal));

                            dataPenjualanViewModel.loadOrderByStatus(4,tanggal);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if(EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onLoaderEvent(LoadOrderEvent event){
        dataPenjualanViewModel.loadOrderByStatus(event.getStatus(),tanggal);
    }
}
