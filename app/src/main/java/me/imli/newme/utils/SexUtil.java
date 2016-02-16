package me.imli.newme.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.ColorRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import java.util.ArrayList;
import java.util.List;

import me.imli.newme.ImApp;
import me.imli.newme.R;
import me.imli.newme.helper.MDTintHelper;
import me.imli.newme.listener.SexSelectedListener;

/**
 * Created by Em on 2015/12/28.
 */
public class SexUtil {

    public static final void selectSex(Context context, SexSelectedListener l) {
        final ChoiceOnClickListener listener = new ChoiceOnClickListener();
        new AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle(R.string.please_select_sex)
                .setSingleChoiceItems(new Sexdapter(context, R.layout.item_sex, android.R.id.text1, Sex.getSexs()), 0, listener)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (l != null) {
                            l.onSelect(false);
                        }
                    }
                })
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SharedPrefUtil.putBoolean(context, context.getString(R.string.sex_is_selected), true);
                        SharedPrefUtil.putString(context, context.getString(R.string.pre_sex), Sex.getSexs().get(listener.getWhich()).tag);
                        if (l != null) {
                            l.onSelect(true);
                        }
                    }
                })
                .create()
                .show();
    }

    /**
     * 是否已经选择过性别
     * @param context
     * @return
     */
    public static final boolean isSelectedSex(Context context) {
        return SharedPrefUtil.getBoolean(context, context.getString(R.string.sex_is_selected), false);
    }

    public static final int getSex(Context context) {
        String tag = SharedPrefUtil.getString(context, context.getString(R.string.pre_sex), Sex.getSexs().get(0).tag);
        if (context.getString(R.string.sex_tag_girl).equals(tag)) {
            return 0;
        } else if (context.getString(R.string.sex_tag_boy).equals(tag)) {
            return 1;
        }
        return -1;
    }

    /**
     *
     */
    private static class ChoiceOnClickListener implements DialogInterface.OnClickListener {

        private int which = 0;
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            this.which = which;
        }

        public int getWhich() {
            return which;
        }
    }

    /**
     * Sex
     */
    public static class Sex {
        public String name;
        public String tag;
        public int color;

        private static List<Sex> SEX_ARR = new ArrayList<>();
        static {
            SEX_ARR.add(new Sex(getString(R.string.sex_boy), getString(R.string.sex_tag_boy), R.color.sex_boy));
            SEX_ARR.add(new Sex(getString(R.string.sex_girl), getString(R.string.sex_tag_girl), R.color.sex_girl));
        }

        public Sex(String name, String tag, @ColorRes int color) {
            this.name = name;
            this.tag = tag;
            this.color = color;
        }

        protected static List<Sex> getSexs() {
            return SEX_ARR;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Sexdapter
     */
    private static class Sexdapter extends ArrayAdapter<Sex> {

        private int mTextId;
        public Sexdapter(Context context, int resource, int textViewResourceId, List<Sex> objects) {
            super(context, resource, textViewResourceId, objects);
            this.mTextId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Sex sex = getItem(position);
            View view = super.getView(position, convertView, parent);
            CheckedTextView textView = (CheckedTextView) view.findViewById(mTextId);
            MDTintHelper.setTint(textView, getContext().getResources().getColor(sex.color));
            textView.setTextColor(getContext().getResources().getColor(sex.color));
            return view;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    private static String getString(int resid) {
        return ImApp.getContext().getString(resid);
    }
}
