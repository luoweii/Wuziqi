package com.luowei.wuziqi;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 骆巍 on 2016/4/1.
 */
public class WuziqiPanel extends View {
    private float panelWidth;
    private float lineHeight;
    private int maxLine;
    private Paint p = new Paint();
    private int color;
    private float lineWidth;

    private Bitmap whitePiece;
    private Bitmap blackPiece;
    private float ratioPieceOfLineHeight = 3 * 1f / 4;

    private boolean isWhite = true;//白棋走
    private ArrayList<Point> whites = new ArrayList<>();
    private ArrayList<Point> blacks = new ArrayList<>();
    private boolean isGameOver;
    private static final int MAX_COUNT_IN_LINE = 5;

    public WuziqiPanel(Context context) {
        this(context, null);
    }

    public WuziqiPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WuziqiPanel);
        maxLine = a.getInteger(R.styleable.WuziqiPanel_wp_maxLine, 10);
        color = a.getColor(R.styleable.WuziqiPanel_wp_color, 0x99000000);
        lineWidth = a.getDimension(R.styleable.WuziqiPanel_wp_lineWidth, 4);
        a.recycle();

        p.setColor(color);
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(lineWidth);

        whitePiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        blackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        panelWidth = w;
        lineHeight = w * 1.0f / maxLine;

        int pieceWidth = (int) (lineHeight * ratioPieceOfLineHeight);
        whitePiece = Bitmap.createScaledBitmap(whitePiece, pieceWidth, pieceWidth, false);
        blackPiece = Bitmap.createScaledBitmap(blackPiece, pieceWidth, pieceWidth, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw board
        for (int i = 0; i < maxLine; i++) {
            float startX = lineHeight / 2;
            float endX = panelWidth - lineHeight / 2;
            float y = (0.5f + i) * lineHeight;
            canvas.drawLine(startX, y, endX, y, p);
            canvas.drawLine(y, startX, y, endX, p);
        }

        //draw pieces
        for (Point p : whites) {
            canvas.drawBitmap(whitePiece, (p.x + (1 - ratioPieceOfLineHeight) / 2) * lineHeight,
                    (p.y + (1 - ratioPieceOfLineHeight) / 2) * lineHeight, null);
        }
        for (Point p : blacks) {
            canvas.drawBitmap(blackPiece, (p.x + (1 - ratioPieceOfLineHeight) / 2) * lineHeight,
                    (p.y + (1 - ratioPieceOfLineHeight) / 2) * lineHeight, null);
        }

        checkGameOver();
    }

    private void checkGameOver() {
        boolean whiteWin = checkFiveInLine(whites);
        boolean blackWin = checkFiveInLine(blacks);
        if (whiteWin || blackWin) {
            isGameOver = true;
            new AlertDialog.Builder(getContext())
                    .setMessage(whiteWin ? "白棋胜利" : "黑棋胜利")
                    .setPositiveButton("确定", null).show();
        }
    }

    private boolean checkFiveInLine(List<Point> points) {
        for (Point p : points) {
            int count = 0;
            //横向
            count = getLineCount(points, p,new Point(1,0));
            if (count >= 5) return true;
            //纵向
            count = getLineCount(points, p,new Point(0,1));
            if (count >= 5) return true;
            //左斜
            count = getLineCount(points, p,new Point(1,1));
            if (count >= 5) return true;
            //右斜
            count = getLineCount(points, p,new Point(-1,1));
            if (count >= 5) return true;
        }
        return false;
    }

    private int getLineCount(List<Point> points, Point p,Point dire) {
        int count = 1;
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(p.x+(dire.x*i), p.y + (dire.y*i)))) count++;
            else break;
        }
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(p.x-(dire.x*i), p.y - (dire.y*i)))) count++;
            else break;
        }
        return count;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameOver) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                Point p = getValidatePoint(event.getX(), event.getY());
                if (whites.contains(p) || blacks.contains(p)) {
                    return true;
                }
                if (isWhite) {
                    whites.add(p);
                } else {
                    blacks.add(p);
                }
                invalidate();
                isWhite = !isWhite;
                break;
        }
        return true;
    }

    private Point getValidatePoint(float x, float y) {
        return new Point((int) (x / lineHeight), (int) (y / lineHeight));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle b = new Bundle();
        b.putParcelable("instance", super.onSaveInstanceState());
        b.putParcelableArrayList("whites", whites);
        b.putParcelableArrayList("blacks", blacks);
        return b;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle b = (Bundle) state;
            whites = b.getParcelableArrayList("whites");
            blacks = b.getParcelableArrayList("blacks");
            super.onRestoreInstanceState(b.getParcelable("instance"));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public void start() {
        whites.clear();
        blacks.clear();
        isGameOver = false;
        invalidate();
    }
}
