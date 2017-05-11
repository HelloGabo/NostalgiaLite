package nostalgia.framework.ui.timetravel;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import nostalgia.framework.EmulatorException;
import nostalgia.framework.R;
import nostalgia.framework.base.Manager;
import nostalgia.framework.ui.gamegallery.GameDescription;

public class TimeTravelDialog extends Dialog implements OnSeekBarChangeListener {

    private ImageView img;
    private TextView label;
    private Manager manager;
    private Bitmap bitmap;
    private GameDescription game;
    private int max = 0;

    public TimeTravelDialog(final Context context, Manager manager,
                            GameDescription game) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.manager = manager;
        this.game = game;
        bitmap = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.dialog_time_travel, null);
        setContentView(content);
        final SeekBar seekBar = (SeekBar) content
                .findViewById(R.id.dialog_time_seek);
        seekBar.setOnSeekBarChangeListener(this);
        Button cancel = (Button) content
                .findViewById(R.id.dialog_time_btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        cancel.setFocusable(true);
        img = (ImageView) content.findViewById(R.id.dialog_time_img);
        label = (TextView) content.findViewById(R.id.dialog_time_label);
        max = manager.getHistoryItemCount() - 1;
        seekBar.setMax(max);
        seekBar.setProgress(max);
        Button ok = (Button) content
                .findViewById(R.id.dialog_time_wheel_btn_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeTravelDialog.this.manager
                        .startGame(TimeTravelDialog.this.game);
                TimeTravelDialog.this.manager.loadHistoryState(max
                        - seekBar.getProgress());

                try {
                    TimeTravelDialog.this.manager.enableCheats(context,
                            TimeTravelDialog.this.game);

                } catch (EmulatorException e) {
                }

                dismiss();
            }
        });
        ok.setFocusable(true);
        TextView title = (TextView) content
                .findViewById(R.id.dialog_time_title);
        manager.pauseEmulation();
        manager.renderHistoryScreenshot(bitmap, 0);
        img.setImageBitmap(bitmap);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        label.setText(String.format("-%02.2fs", (max - progress) / 4f));
        manager.renderHistoryScreenshot(bitmap, max - progress);
        img.setImageBitmap(bitmap);
        img.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

}
