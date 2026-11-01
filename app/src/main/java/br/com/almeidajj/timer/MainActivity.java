package br.com.almeidajj.timer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Tela cheia com o timer (HTML embutido em assets). Não usa internet. */
public class MainActivity extends Activity {
    private WebView web;
    // Voltar: na configuração sai do app; nas outras telas pausa / volta ao menu
    private static final String BACK_JS =
        "(function(){if(window.androidBack)return window.androidBack();"
        + "var s=document.getElementById('setup');if(s&&!s.hidden)return 'exit';"
        + "document.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape'}));return 'handled'})()";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mantém a TV ligada durante o treino
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(11, 11, 10));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);                 // guarda a última configuração
        s.setMediaPlaybackRequiresUserGesture(false); // som do gongo sem bloqueio
        s.setAllowFileAccess(true);
        web.setWebViewClient(new WebViewClient());
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        setContentView(web);
        hideBars();
        web.loadUrl("file:///android_asset/index.html");
        web.requestFocus();
    }

    private void hideBars() {
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        // Botão Voltar do controle: o app decide (pausar, voltar ao menu ou sair)
        if (e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (e.getAction() == KeyEvent.ACTION_UP) {
                web.evaluateJavascript(BACK_JS, r -> {
                    if (r == null || r.contains("exit")) finish();
                });
            }
            return true;
        }
        // Botão Play/Pause do controle: pausa ou continua
        int k = e.getKeyCode();
        if (k == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || k == KeyEvent.KEYCODE_MEDIA_PLAY || k == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (e.getAction() == KeyEvent.ACTION_DOWN) {
                web.evaluateJavascript("document.dispatchEvent(new KeyboardEvent('keydown',{key:'MediaPlayPause'}))", null);
            }
            return true;
        }
        if (k == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
            if (e.getAction() == KeyEvent.ACTION_DOWN) {
                web.evaluateJavascript("document.dispatchEvent(new KeyboardEvent('keydown',{key:'MediaFastForward'}))", null);
            }
            return true;
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    protected void onPause() { super.onPause(); web.onPause(); }

    @Override
    protected void onResume() { super.onResume(); web.onResume(); hideBars(); }
}
