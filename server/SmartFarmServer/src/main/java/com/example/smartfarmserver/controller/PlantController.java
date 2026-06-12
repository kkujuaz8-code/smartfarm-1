package com.example.smartfarmserver.controller;

import com.example.smartfarmserver.dto.PlantResponse;
import com.example.smartfarmserver.entity.CustomDictionary;
import com.example.smartfarmserver.repository.CustomDictionaryRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plants")
@RequiredArgsConstructor
public class PlantController {

    @Value("${rda.plant.key}")
    private String apiKey;

    private final CustomDictionaryRepository customDictionaryRepository;

    @GetMapping
    public List<PlantResponse> getAllPlants() {
        // 공공 API 목록 가져오기 (상세 정보까지 포함하도록 callPlantApi 수정됨)
        List<PlantResponse> rdaList = callPlantApi("");
        
        // 내 사전 합치기
        List<PlantResponse> customList = customDictionaryRepository.findAll().stream()
                .map(this::convertToPlantResponse)
                .collect(Collectors.toList());
        
        rdaList.addAll(customList);
        return rdaList;
    }

    @GetMapping("/search")
    public List<PlantResponse> searchPlant(@RequestParam(name = "name") String name) {
        // 공공 API 검색 (상세 정보까지 포함하도록 callPlantApi 수정됨)
        List<PlantResponse> rdaList = callPlantApi(name);
        
        // 내 사전 검색
        List<PlantResponse> customList = customDictionaryRepository.findByNameContaining(name).stream()
                .map(this::convertToPlantResponse)
                .collect(Collectors.toList());
        
        rdaList.addAll(customList);
        return rdaList;
    }

    private PlantResponse convertToPlantResponse(CustomDictionary entity) {
        // 커스텀 데이터 변환 (이전과 동일)
        return new PlantResponse(
            entity.getName(),
            entity.getDescription(), 
            "내 사전 등록 데이터",      // info
            entity.getImageUrl(),
            entity.getWaterCycle() != null ? entity.getWaterCycle() : "정보 없음",
            entity.getRepotCycle() != null ? entity.getRepotCycle() : "정보 없음",
            entity.getSunlight() != null ? entity.getSunlight() : "정보 없음"
        );
    }

    /**
     * 🌟 공공 API 호출 메서드 (상세 정보까지 가져오도록 대폭 수정)
     */
    private List<PlantResponse> callPlantApi(String keyword) {
        List<PlantResponse> resultList = new ArrayList<>();
        try {
            // 1. 목록 API 호출하여 식물 리스트와 cntntsNo(고유번호) 가져오기
            StringBuilder urlBuilder = new StringBuilder("http://api.nongsaro.go.kr/service/garden/gardenList");
            urlBuilder.append("?apiKey=").append(apiKey).append("&numOfRows=50&pageNo=1");
            if (keyword != null && !keyword.trim().isEmpty()) {
                urlBuilder.append("&sType=sCntntsSj&sText=").append(URLEncoder.encode(keyword, "UTF-8"));
            }

            String listXml = sendGetRequest(urlBuilder.toString());
            Document listDoc = XmlParserUtil.parseXml(listXml);
            NodeList nList = listDoc.getElementsByTagName("item");

            for (int i = 0; i < nList.getLength(); i++) {
                Element element = (Element) nList.item(i);
                String cntntsNo = XmlParserUtil.getTagValue("cntntsNo", element); // 고유번호
                String name = XmlParserUtil.getTagValue("cntntsSj", element);     // 식물명
                String scName = XmlParserUtil.getTagValue("plntbneNm", element);  // 학명
                String imgPath = XmlParserUtil.getTagValue("rtnFileUrl", element); // 이미지

                if (name.isEmpty() || cntntsNo.isEmpty()) continue;

                // 2. 🌟 각 식물에 대해 상세 정보 API 호출 (가장 중요)
                String dtlUrl = "http://api.nongsaro.go.kr/service/garden/gardenDtl?apiKey=" + apiKey + "&cntntsNo=" + cntntsNo;
                String dtlXml = sendGetRequest(dtlUrl);
                Document dtlDoc = XmlParserUtil.parseXml(dtlXml);
                Element dtlElement = (Element) dtlDoc.getElementsByTagName("item").item(0);

                // 상세 정보 추출 (태그명은 농사로 API 명세서에 맞춰 수정 필요)
                String water = XmlParserUtil.getTagValue("waterCycleTag", dtlElement); // 예시 태그명
                String repot = XmlParserUtil.getTagValue("repotCycleTag", dtlElement);  // 예시 태그명
                String sun = XmlParserUtil.getTagValue("sunlightTag", dtlElement);     // 예시 태그명

                // 이미지 URL 전처리 (이전과 동일)
                String fullImgUrl = "";
                if (imgPath != null && !imgPath.isEmpty()) {
                    String firstImg = imgPath.split("\\|")[0];
                    fullImgUrl = firstImg.startsWith("http") ? firstImg.replace("http://", "https://") : "https://www.nongsaro.go.kr/" + firstImg;
                }

                // 3. 🌟 추출한 상세 정보를 담아 PlantResponse 생성
                resultList.add(new PlantResponse(
                        name, 
                        "학명: " + scName, 
                        "공공데이터 정보", 
                        fullImgUrl, 
                        (water != null && !water.isEmpty()) ? water : "정보 없음", 
                        (repot != null && !repot.isEmpty()) ? repot : "정보 없음", 
                        (sun != null && !sun.isEmpty()) ? sun : "정보 없음"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * HTTP GET 요청을 보내고 응답을 문자열로 반환하는 공통 메서드
     */
    private String sendGetRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/xml");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        conn.disconnect();
        return sb.toString();
    }
}