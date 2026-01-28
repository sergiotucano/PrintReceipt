package br.com.mbd.print_receipt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 12/08/2016.
 */
public class ReceiptBuilder {
    List<IDrawItem> listItens = new ArrayList<>();
    private int backgroundColor = Color.WHITE;
    private float textSize;
    private int color = Color.BLACK;
    private int width;
    private int marginTop, marginBottom, marginLeft, marginRight;
    private Typeface typeface;
    private Paint.Align align = Paint.Align.LEFT;

    public ReceiptBuilder(int width) {
        this.width = width;
    }

    public ReceiptBuilder setTextSize(float textSize) {
        this.textSize = textSize;
        return this;
    }

    public ReceiptBuilder setBackgroudColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public ReceiptBuilder setColor(int color) {
        this.color = color;
        return this;
    }

    public ReceiptBuilder setTypeface(Context context, String typefacePath) {
        typeface = Typeface.createFromAsset(context.getAssets(), typefacePath);
        return this;
    }

    public ReceiptBuilder setDefaultTypeface() {
        typeface = null;
        return this;
    }

    public ReceiptBuilder setAlign(Paint.Align align) {
        this.align = align;
        return this;
    }

    public ReceiptBuilder setMargin(int margin) {
        this.marginLeft = margin;
        this.marginRight = margin;
        this.marginTop = margin;
        this.marginBottom = margin;
        return this;
    }

    public ReceiptBuilder setMargin(int marginTopBottom, int marginLeftRight) {
        this.marginLeft = marginLeftRight;
        this.marginRight = marginLeftRight;
        this.marginTop = marginTopBottom;
        this.marginBottom = marginTopBottom;
        return this;
    }

    public ReceiptBuilder setMarginLeft(int margin) {
        this.marginLeft = margin;
        return this;
    }

    public ReceiptBuilder setMarginRight(int margin) {
        this.marginRight = margin;
        return this;
    }

    public ReceiptBuilder setMarginTop(int margin) {
        this.marginTop = margin;
        return this;
    }

    public ReceiptBuilder setMarginBottom(int margin) {
        this.marginBottom = margin;
        return this;
    }

    public ReceiptBuilder addText(String text) {
        return addText(text, true);
    }

    public ReceiptBuilder addText(String text, Boolean newLine) {

        final String finalText = text;
        final float finalTextSize = this.textSize;
        final int finalColor = this.color;
        final int finalBackground = this.backgroundColor;
        final Typeface finalTypeface = this.typeface;
        final Paint.Align finalAlign = this.align;
        final int finalWidth = this.width - marginLeft - marginRight;
        final boolean finalNewLine = newLine;

        IDrawItem item = new IDrawItem() {

            @Override
            public int getHeight() {
                if (!finalNewLine) return 0;

                Paint p = new Paint();
                p.setTextSize(finalTextSize);
                Paint.FontMetrics fm = p.getFontMetrics();
                return (int) (fm.bottom - fm.top) + 16;
            }

            @Override
            public void drawOnCanvas(Canvas canvas, float x, float y) {
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setTextSize(finalTextSize);
                paint.setTextAlign(finalAlign);

                if (finalTypeface != null) {
                    paint.setTypeface(finalTypeface);
                }

                // Identifica a altura total reservada para esta linha
                int itemHeight = getHeight();
                Paint.FontMetrics fm = paint.getFontMetrics();

                // ✅ 1. DESENHAR O FUNDO PRIMEIRO
                if (finalBackground != Color.WHITE) {
                    Paint bg = new Paint();
                    bg.setStyle(Paint.Style.FILL);
                    bg.setColor(finalBackground);
                    // O fundo começa em 'y' e vai até 'y + itemHeight'
                    // Usamos 'width' (largura total) para a tarja cobrir o papel todo
                    canvas.drawRect(0, y, width, y + itemHeight, bg);
                }

                // ✅ 2. DEFINIR COR DO TEXTO (Contraste)
                // Se o fundo for escuro, o texto precisa ser branco
                if (finalBackground == Color.BLACK) {
                    paint.setColor(Color.WHITE);
                } else {
                    paint.setColor(finalColor);
                }

                // ✅ 3. CALCULAR A BASELINE (Onde o texto "senta")
                // Centraliza o texto verticalmente dentro do itemHeight
                float centerY = y + (itemHeight / 2f);
                float baseline = centerY - (fm.ascent + fm.descent) / 2f;

                // ✅ 4. CALCULAR O X (Alinhamento Horizontal)
                float finalX = x;
                if (finalAlign == Paint.Align.LEFT) {
                    finalX = marginLeft;
                } else if (finalAlign == Paint.Align.CENTER) {
                    finalX = width / 2f;
                } else if (finalAlign == Paint.Align.RIGHT) {
                    finalX = width - marginRight;
                }

                canvas.drawText(finalText, finalX, baseline, paint);
            }
        };

        listItens.add(item);
        return this;
    }

    public ReceiptBuilder addImage(Bitmap bitmap) {
        DrawImage drawerImage = new DrawImage(bitmap);
        if (align != null) {
            drawerImage.setAlign(align);
        }
        listItens.add(drawerImage);
        return this;
    }

    public ReceiptBuilder addItem(IDrawItem item) {
        listItens.add(item);
        return this;
    }

    public ReceiptBuilder addBlankSpace(int heigth) {
        listItens.add(new DrawBlankSpace(heigth));
        return this;
    }

    public ReceiptBuilder addParagraph() {
        listItens.add(new DrawBlankSpace((int) textSize));
        return this;
    }

    public ReceiptBuilder addLine() {
        return addLine(width - marginRight - marginLeft);
    }

    public ReceiptBuilder addLine(int size) {
        DrawLine line = new DrawLine(size);
        line.setAlign(align);
        line.setColor(color);
        listItens.add(line);
        return this;
    }

    private int getHeight() {
        int height = 5 + marginTop + marginBottom;
        for (IDrawItem item : listItens) {
            height += item.getHeight();
        }
        return height;
    }

    private Bitmap drawImage() {
        Bitmap image = Bitmap.createBitmap(width - marginRight - marginLeft, getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawColor(backgroundColor);
        float size = marginTop;
        for (IDrawItem item : listItens) {
            item.drawOnCanvas(canvas, 0, size);
            size += item.getHeight();
        }
        return image;
    }

    public Bitmap build() {
        Bitmap image = Bitmap.createBitmap(width, getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        canvas.drawColor(backgroundColor);
        canvas.drawBitmap(drawImage(), marginLeft, 0, paint);
        return image;
    }

}
