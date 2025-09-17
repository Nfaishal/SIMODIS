package com.example.simodis.ui.data;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.simodis.ui.infografis.InfografisFragment;
import com.example.simodis.ui.datasektoral.DataSektoralFragment;
import com.example.simodis.ui.tabelstatis.TabelStatisFragment;

public class DataPagerAdapter extends FragmentStateAdapter {

    public DataPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TabelStatisFragment(); // Halaman untuk data internal BPS
            case 1:
                return new DataSektoralFragment(); // Halaman Data Sektoral Anda
            case 2:
                return new InfografisFragment(); // Halaman Infografis Anda
            default:
                return new TabelDinamisFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Karena kita punya 3 tab
    }
}
