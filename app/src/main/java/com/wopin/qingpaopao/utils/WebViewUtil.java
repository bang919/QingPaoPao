package com.wopin.qingpaopao.utils;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.net.http.SslError;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.ViewTreeObserver;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebViewUtil {

    public static void loadWebView(final FragmentActivity fragmentActivity, final WebView webView, final String content) {
        fragmentActivity.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            void onDestroy(LifecycleOwner lifecycleOwner) {
                webView.loadUrl("");
                webView.destroy();
                fragmentActivity.getLifecycle().removeObserver(this);
            }
        });
        fragmentActivity.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            void onCreate(LifecycleOwner lifecycleOwner) {
                webView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        loadWebView(webView, content);
                        webView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
                fragmentActivity.getLifecycle().removeObserver(this);
            }
        });
    }

    public static void loadWebView(final Fragment fragment, final WebView webView, final String content) {
        fragment.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            void onDestroy(LifecycleOwner lifecycleOwner) {
                webView.loadUrl("");
                webView.destroy();
                fragment.getLifecycle().removeObserver(this);
            }
        });
        fragment.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            void onCreate(LifecycleOwner lifecycleOwner) {
                webView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        loadWebView(webView, content);
                        webView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
                fragment.getLifecycle().removeObserver(this);
            }
        });
    }

    private static void loadWebView(WebView webView, String content) {
        webView.loadData(getNewContent(content), "text/html; charset=UTF-8", null);
        WebSettings settings = webView.getSettings();
        // 如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //等待证书响应
                handler.proceed();
            }
        });
    }

    /**
     * 将html文本内容中包含img标签的图片，宽度变为屏幕宽度，高度根据宽度比例自适应
     **/
    private static String getNewContent(String htmltext) {
        try {
            Document doc = Jsoup.parse(htmltext);
            Elements elements = doc.getElementsByTag("img");
            for (Element element : elements) {
                element.attr("width", "100%").attr("height", "auto");
            }

            return "<html><body>" + doc.toString() + "</body></html>";
        } catch (Exception e) {
            return "<html><body>" + htmltext + "</body></html>";
        }
    }
}
