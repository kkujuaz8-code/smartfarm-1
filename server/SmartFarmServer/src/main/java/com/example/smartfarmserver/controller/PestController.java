package com.example.smartfarmserver.controller;

import com.example.smartfarmserver.dto.PestResponse;
import com.example.smartfarmserver.entity.PestDictionary;
import com.example.smartfarmserver.repository.PestDictionaryRepository;
import com.example.smartfarmserver.util.XmlParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/pests")
@RequiredArgsConstructor
public class PestController {

    @Value("${rda.pest.key}")
    private String apiKey;

    private final PestDictionaryRepository pestRepository;

    @GetMapping
    public List<PestResponse> getAllPests() {
        return callRdaApi("병");
    }

    @GetMapping("/search")
    public List<PestResponse> searchPest(@RequestParam(name = "keyword", defaultValue = "") String keyword) {
        return callRdaApi(keyword);
    }

    @GetMapping("/custom")
    public List<PestDictionary> searchCustomPest(@RequestParam(name = "keyword", defaultValue = "") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return pestRepository.findAll();
        }
        return pestRepository.findByNameContaining(keyword);
    }

    private List<PestResponse> callRdaApi(String keyword) {
        List<PestResponse> resultList = new ArrayList<>();
        try {
            StringBuilder urlBuilder = new StringBuilder("http://ncpms.rda.go.kr/npmsAPI/service");
            urlBuilder.append("?apiKey=").append(apiKey);
            urlBuilder.append("&serviceCode=SVC01");
            urlBuilder.append("&serviceType=AA001");
            urlBuilder.append("&displayCount=50");
            urlBuilder.append("&startPoint=1");
            urlBuilder.append("&sickNameKor=").append(URLEncoder.encode(keyword, "UTF-8"));

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            Document doc = XmlParserUtil.parseXml(sb.toString());
            NodeList nList = doc.getElementsByTagName("item");

            for (int i = 0; i < nList.getLength(); i++) {
                Element element = (Element) nList.item(i);

                String pestName = XmlParserUtil.getTagValue("sickNameKor", element);
                if (pestName.isEmpty()) pestName = XmlParserUtil.getTagValue("sicknsKorNm", element);
                String cropName = XmlParserUtil.getTagValue("cropName", element);
                
                // 🌟 이미지 태그 추출 (여러 경우의 수 대비)
                String imgPath = XmlParserUtil.getTagValue("thumbImg", element);
                if (imgPath.isEmpty()) imgPath = XmlParserUtil.getTagValue("imgUrl", element);
                if (imgPath.isEmpty()) imgPath = XmlParserUtil.getTagValue("image", element);

                if (!pestName.isEmpty()) {
                    // 서버 콘솔에서 데이터 확인용
                    System.out.println("DEBUG >> " + pestName + " URL: " + imgPath);
                    resultList.add(new PestResponse(pestName, cropName, "상세정보", imgPath));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
}