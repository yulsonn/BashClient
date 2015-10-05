package ru.loftschool.bashclient.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ru.loftschool.bashclient.R;
import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.BundleConstants;
import ru.loftschool.bashclient.ui.fragments.FavoriteStoriesFragment;

public class DelFromFavoriteDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getString(R.string.df_title);
        String btnOk = getString(R.string.df_delete);
        String btnCancel = getString(R.string.df_cancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title)
                .setPositiveButton(btnOk, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        long storyId = getArguments().getLong(BundleConstants.ARG_ID);
                        int position = getArguments().getInt(BundleConstants.ARG_POSITION);
                        Story story = Story.select(storyId);
                        story.favorite = false;
                        story.save();
                        FavoriteStoriesFragment.getAdapter().removeItem(position);
                    }
                })
                .setNegativeButton(btnCancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }
}
