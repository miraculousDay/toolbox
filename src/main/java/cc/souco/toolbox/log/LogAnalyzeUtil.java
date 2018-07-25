package cc.souco.toolbox.log;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

public class LogAnalyzeUtil {
    private static final String LOG_FILE_PATH = "D:\\Logs\\xj";
    private static final List<String> analyse = Lists.newArrayList("ip", "contentType", "url", "httpMethod", "userAgent");
    private static UserAgentAnalyzer uaa = null;

    public static void main(String[] args) {
        String fileName = "wt.log_2018-07-17.log";

        Map<String, Map<String, Integer>> analyzeResult = analyzeSecond(analyze(LOG_FILE_PATH + File.separator + fileName, analyse));

        analyzeResult.remove("userAgent");
        analyzeResult.remove("url");
        analyzeResult.remove("ip");
        analyzeResult.remove("contentType");
        analyzeResult.remove("urlSuffix");

        Set<Map.Entry<String, Map<String, Integer>>> mapEntries = analyzeResult.entrySet();
        for (Map.Entry<String, Map<String, Integer>> analyzeMap : mapEntries) {
            System.out.println();
            System.out.println(analyzeMap.getKey());
            System.out.println(String.join("", Collections.nCopies(120, "-")));
            Map<String, Integer> analysis = analyzeMap.getValue();
            ArrayList<Map.Entry<String, Integer>> analysisList = new ArrayList<>(analysis.entrySet());
            analysisList.sort((o1, o2) -> o2.getValue() - o1.getValue());
            for (Map.Entry<String, Integer> analyze : analysisList) {
                System.out.println(analyze.getKey() + " : " + analyze.getValue());
            }
        }
    }

    /**
     * 对日志做一级分类统计
     * @param filePath 文件路径
     * @param analysis 分析的字段
     * @return 分析结果
     */
    public static Map<String, Map<String, Integer>> analyze(String filePath, List<String> analysis) {
        HashMap<String, Map<String, Integer>> result = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(filePath)), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!StringUtils.isEmpty(line) && line.contains("  INFO ")) {
                    for (String analyzeKey : analysis) {
                        int matcherStart = line.indexOf(analyzeKey + "[");
                        int matcherEnd = line.indexOf("]", matcherStart);
                        if (matcherStart > -1 && matcherEnd > -1) {
                            String matcher = line.substring(matcherStart + analyzeKey.length() + 1, matcherEnd);
                            Map<String, Integer> analyzeKeyMap = result.getOrDefault(analyzeKey, new HashMap<>());
                            Integer count = analyzeKeyMap.getOrDefault(matcher, 0);
                            analyzeKeyMap.put(matcher, ++count);
                            result.put(analyzeKey, analyzeKeyMap);
                        }
                    }
                }
            }
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Maps.newHashMap();
    }

    /**
     * 对部分统计做二次处理
     * @param firstAnalyze 第一次分类处理的结果
     * @return 分析结果，两层Map
     */
    public static Map<String, Map<String, Integer>> analyzeSecond(Map<String, Map<String, Integer>> firstAnalyze){
        Map<String, Map<String, Integer>> result = Maps.newHashMap();

        Set<Map.Entry<String, Map<String, Integer>>> mapEntries = firstAnalyze.entrySet();
        for (Map.Entry<String, Map<String, Integer>> analyzeMap : mapEntries) {
            String analyzeKey = analyzeMap.getKey();
            Set<Map.Entry<String, Integer>> analyzeKeyValue = analyzeMap.getValue().entrySet();

            // url后缀统计
            if ("url".equals(analyzeKey)) {
                Map<String, Integer> urlSuffixCount = new HashMap<>();
                for (Map.Entry<String, Integer> entry : analyzeKeyValue) {
                    String url = entry.getKey();
                    String suffix = url;
                    if (url.contains(".")) {
                        suffix = url.substring(url.lastIndexOf("."));
                    }
                    Integer suffixCount = urlSuffixCount.getOrDefault(suffix, 0);
                    urlSuffixCount.put(suffix, suffixCount + entry.getValue());
                }
                result.put("urlSuffix", urlSuffixCount);
            }

            // 用户请求头分析
            List<String> agentAnalyzeList = Lists.newArrayList(UserAgent.OPERATING_SYSTEM_NAME,
                    "OperatingSystemNameVersion", UserAgent.AGENT_NAME, "AgentNameVersionMajor");
            if ("userAgent".equals(analyzeKey)) {
                for (Map.Entry<String, Integer> entry : analyzeKeyValue) {
                    String userAgent = entry.getKey();
                    UserAgent agent = getUserAgentAnalyzer().parse(userAgent);
                    for (String agentAnalyze : agentAnalyzeList) {
                        String agentField = agent.get(agentAnalyze).getValue();
                        Map<String, Integer> agentCount = result.getOrDefault(agentAnalyze, Maps.newHashMap());
                        Integer count = agentCount.getOrDefault(agentField, 0);
                        agentCount.put(agentField, count + entry.getValue());
                        result.put(agentAnalyze, agentCount);
                    }
                }
            }
        }
        firstAnalyze.putAll(result);
        return firstAnalyze;
    }

    private static UserAgentAnalyzer getUserAgentAnalyzer(){
        if (null == uaa) {
            uaa = UserAgentAnalyzer.newBuilder().hideMatcherLoadStats().withCache(25000).build();
        }
        return uaa;
    }

}