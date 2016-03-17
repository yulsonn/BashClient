package ru.loftschool.bashclient.ui;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ru.loftschool.bashclient.database.models.Story;
import ru.loftschool.bashclient.ui.listeners.LinkClickListener;

public class MyWebViewClient extends WebViewClient {

    private LinkClickListener linkClickListener;

    public MyWebViewClient(LinkClickListener linkClickListener) {
        this.linkClickListener = linkClickListener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("http://zadolba.li/story")) {
            String num = url.substring(url.lastIndexOf("/") + 1, url.length());
            Story story = Story.selectByNum(Integer.parseInt(num));

            if (story != null) {
                //внутренние ссылки открываем в рамках нашего приложения
                linkClickListener.onLinkClicked(story.getId());
            } else {
                //если истоии нет в базе - открываем в браузере
                openExternalUrl(view, url);
            }

            return true;

        } else {
            //внешние ссылки открываем в браузере
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(intent);
        }

        return true;
    }

    private void openExternalUrl(WebView view, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
    }
}


