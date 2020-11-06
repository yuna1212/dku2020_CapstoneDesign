package com.searcher.booksearch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.searcher.booksearch.R;

public class ScanBarcode extends AppCompatActivity {

    public static final int REQUEST_CODE_GO_TO_MY_BOOK_LIST = 105;

    private String ISBN;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_result_activity);
        context = this;

        // 액션바 숨김
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // 바코드 스캔
        IntentIntegrator barcodeScan = new IntentIntegrator(this);
        barcodeScan.setOrientationLocked(false);    // 휴대폰 방향에 따라 가로, 세로로 자동 변경
        barcodeScan.setPrompt("도서 뒷면의 바코드를 사각형 안에 비춰주세요");  //바코드 안의 텍스트 설정
        barcodeScan.setBeepEnabled(false);  //바코드 인식시 소리 여부
        barcodeScan.initiateScan();

        // 내 관심 도서 목록으로 이동하는 button
        Button goToListButton = (Button) findViewById(R.id.goToListButton);
        goToListButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyBookList.class); //관심 도서.class연결
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent, REQUEST_CODE_GO_TO_MY_BOOK_LIST); //REQUEST_CODE_관심도서
            }
        });

        // 내 관심 도서 목록에 해당 도서 추가하는 button
        ImageButton addToListButton = (ImageButton) findViewById(R.id.addToListButton);
        addToListButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ManageDatabase manageDatabase = new ManageDatabase(context);

                // 해당 도서가 데이터베이스에 존재하는지 검사
                if (manageDatabase.isDataExist(ISBN)) {     // 존재할 경우
                    Toast.makeText(context, "내 관심 도서 목록에 해당 도서가 존재합니다.", Toast.LENGTH_LONG).show();
                }

                else {      // 존재하지 않을 경우
                    manageDatabase.insertData(ISBN);    // 데이터베이스에 해당 도서 추가
                    Toast.makeText(context, "내 관심 도서 목록에 추가되었습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {  // 취소 누른 경우 메뉴 선택 페이지로 이동
                Intent intent = new Intent(getApplicationContext(), SelectMenu.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                ISBN = result.getContents();    // 바코드 스캔 결과값을 ISBN 변수에 저장

                // 리뷰 크롤링
                WebView search_webView = findViewById(R.id.searchWebsite);
                TextView review_textView = findViewById(R.id.bookReviews);
                CrawlingReviews reviews = new CrawlingReviews(search_webView, review_textView);
                reviews.runCrawling(ISBN);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}