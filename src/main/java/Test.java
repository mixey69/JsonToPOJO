import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class Test {
    public static void main(String... args) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder input = new StringBuilder("");
        System.out.print("Enter valid JSON object: ");
        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            if(!input.toString().isEmpty() && nextLine.equals("")){
                break;
            }
            input.append(nextLine);
        }
        String json = input.toString();
        String pojo = parseJsonToPOJO(json);
        System.out.print(pojo);
    }

    public static String parseJsonToPOJO(String json) {
        StringBuilder result = new StringBuilder();
        try {
            JSONObject object = new JSONObject(json);

            Set<String> keySet = object.keySet();
            List<String> keyList = new ArrayList<>(keySet);
            try {
                List<String> sortedKeyList = getKeyList(json);
                Collections.sort(keyList, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return sortedKeyList.indexOf(o1) - sortedKeyList.indexOf(o2);
                    }
                });
            } catch (Exception e) {
                System.err.print(e.getMessage());
            }
            for (String key : keyList) {
                Object element = object.get(key);

                boolean needsSerializedName = key.contains("_");
                String name = key.toString();
                if (needsSerializedName) {
                    while (name.contains("_")) {
                        int dashIndex = name.indexOf('_');
                        char charToUpper = name.charAt(dashIndex + 1);
                        name = name.substring(0, dashIndex) + Character.toUpperCase(charToUpper) + name.substring(dashIndex + 2);
                    }
                }

                String type = "";
                if (element == null) {
                    json = json.replaceAll("\n", "");
                    type = String.class.getSimpleName();
                } else if (object.optBoolean(key)) {
                    type = Boolean.class.getSimpleName();
                } else if (!Double.isNaN(object.optDouble(key))) {
                    type = Double.class.getSimpleName();
                } else if (!object.optString(key).equals("")) {
                    type = String.class.getSimpleName();
                } else if (object.optJSONObject(key) != null) {
                    //todo parse inner objects
                    type = Object.class.getSimpleName();
                } else {
                    type = String.class.getSimpleName();
                }

                if (needsSerializedName) {
                    result.append("@SerializedName(\"").append(key).append("\")\n");
                }
                result.append("public ").append(type).append(" ").append(name).append(";");
                if (element != null) {
                    result.append(" //").append(element.toString());
                }
                result.append("\n");
            }
        } catch (JSONException e) {
            System.err.print(e.getMessage());
        }


        return result.toString();
    }

    public static List<String> getKeyList(String json) {
        json = json.replaceAll("\n", ""); //removing new lines
        json = json.replaceAll(" ", ""); //removing spaces
        json = json.replaceAll("\\s?:\\[.*?],", ", "); //removing arrays
        json = json.replaceAll(":\\s?\\{.*?},", ", "); //removing objects
        json = json.replaceAll(":\\s?\"\\{.*?}\",", ", "); //removing string jsons
        json = json.replaceAll(":\\s?\".*?\",", ", "); //removing strings
        json = json.replaceAll(":.*?,", ", "); //removing numbers and nulls
        json = json.substring(0, json.lastIndexOf("\":")) + "\"]";//removing the last object
        json = json.replace("{", "[");

        System.out.println(json);
        JSONArray jsonArray = new JSONArray(json);
        String[] strArr = new String[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) {
            strArr[i] = jsonArray.getString(i);
        }
        return Arrays.asList(strArr);
    }
}
