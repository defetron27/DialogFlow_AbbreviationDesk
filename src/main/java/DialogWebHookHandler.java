import Models.AbbreResponse;
import Models.AbbreResults;
import Models.AbbreResultsResult;
import Utils.Constant;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.XML;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

public class DialogWebHookHandler implements RequestStreamHandler
{
    private JSONParser jsonParser = new JSONParser();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String jsonResponseString;

        String fallbackMessage = "Oops.. there was some server or internal problem, don't worry please say that name again.";

        try
        {
            JSONObject jsonRequestObject = (JSONObject) jsonParser.parse(bufferedReader);

            if (jsonRequestObject.get(Constant.queryResult) != null)
            {
                JSONObject jsonQueryResult = (JSONObject) jsonRequestObject.get(Constant.queryResult);

                if (jsonQueryResult.get(Constant.intent) != null)
                {
                    JSONObject jsonIntent = (JSONObject) jsonQueryResult.get(Constant.intent);

                    if (jsonIntent.get(Constant.displayName) != null)
                    {
                        String jsonDisplayName = (String) jsonIntent.get(Constant.displayName);

                        switch (jsonDisplayName)
                        {
                            case "GetAbbreviationName" :

                                if (jsonQueryResult.get(Constant.parameters) != null)
                                {
                                    JSONObject jsonParameters = (JSONObject) jsonQueryResult.get(Constant.parameters);

                                    if (jsonParameters.get(Constant.name) != null)
                                    {
                                        String name = (String) jsonParameters.get(Constant.name);

                                        if (name != null && !name.equals("") && !name.equals("null"))
                                        {
                                            String abWithCategoryResponse = getResponseForName(name);

                                            String fallBackMessageForName = "I could not find any abbreviation or acronym for \"" + name + ". Please say correct or another abbreviation or acronym name";

                                            AbbreResponse response = new Gson().fromJson(abWithCategoryResponse,AbbreResponse.class);

                                            if (response != null)
                                            {
                                                AbbreResults abbreResults = response.getResults();

                                                if (abbreResults != null)
                                                {
                                                    List<AbbreResultsResult> resultsResult = abbreResults.getResult();

                                                    if (resultsResult != null && resultsResult.size() > 0)
                                                    {
                                                        StringBuilder stringBuilder = new StringBuilder();

                                                        for(int i=0; i<resultsResult.size(); i++)
                                                        {
                                                            if (i == resultsResult.size() - 1)
                                                            {
                                                                stringBuilder.append(String.valueOf(resultsResult.size())).append(". ");
                                                            }
                                                            else
                                                            {
                                                                stringBuilder.append(String.valueOf(i + 1)).append(". ");
                                                            }

                                                            AbbreResultsResult result = resultsResult.get(i);

                                                            if (result.getDefinition() != null)
                                                            {
                                                                stringBuilder.append("Definition : ").append(result.getDefinition()).append(",\n");
                                                            }

                                                            if (result.getCategoryname() != null)
                                                            {
                                                                stringBuilder.append("Category : ").append(result.getCategoryname()).append(",\n");
                                                            }

                                                            if (result.getParentcategoryname() != null)
                                                            {
                                                                stringBuilder.append("Parent Category : ").append(result.getParentcategoryname()).append(".\n");
                                                            }
                                                        }

                                                        String finalMessage = "Abbreviations of " + name + " are listed below : " + stringBuilder.toString() + ". If you want to get abbreviation or acronym for another name, simply say \" 'your abbreviation or acronym name' \"";

                                                        JSONObject jsonObject = getTextResponseForName(finalMessage);

                                                        jsonResponseString = jsonObject.toString();
                                                    }
                                                    else
                                                    {
                                                        JSONObject jsonObject = getTextResponseForName(fallBackMessageForName);

                                                        jsonResponseString = jsonObject.toString();
                                                    }
                                                }
                                                else
                                                {
                                                    JSONObject jsonObject = getTextResponseForName(fallBackMessageForName);

                                                    jsonResponseString = jsonObject.toString();
                                                }
                                            }
                                            else
                                            {
                                                JSONObject jsonObject = getTextResponseForName(fallBackMessageForName);

                                                jsonResponseString = jsonObject.toString();
                                            }
                                        }
                                        else
                                        {
                                            JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                                            jsonResponseString = jsonObject.toString();
                                        }
                                    }
                                    else
                                    {
                                        JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                                        jsonResponseString = jsonObject.toString();
                                    }
                                }
                                else
                                {
                                    JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                                    jsonResponseString = jsonObject.toString();
                                }

                                break;

                            case "Default Welcome Intent" :

                                String welcomeResponse = "Hi, welcome to Abbreviation Desk. It's a pleasure to talk to you. " +
                                        "If you want abbreviation or acronym of any name, i can give abbreviation or acronym of that name. " +
                                        "If you want more instructions or help simple say 'help' " +
                                        "Ok, now you can start to say any abbreviation or acronym name";

                                JSONObject jsonObject = getTextResponseForName(welcomeResponse);

                                jsonResponseString = jsonObject.toString();

                                break;
                            case "Default Help Intent" :

                                String helpResponse = "It pleasure to help you. \n" +
                                        "If you have any doubts or you don't know how to ask to 'Abbreviation Desk', don't worry. \n" +
                                        "I clarify your doubts. If you tell any abbreviation or acronym name, i can give abbreviation or acronym of that name." +
                                        "I can give some examples to how to ask to Abbreviation Desk, ok now let's start, you say \"ASAP\" and category medical, i can give the definition,category name and parent category name." +
                                        "Ok, now you can start to say any abbreviation or acronym name ";

                                JSONObject helpObject = getTextResponseForName(helpResponse);

                                jsonResponseString = helpObject.toString();

                                break;
                            default:

                                JSONObject jsonDefaultObject = getTextResponseForName(fallbackMessage);

                                jsonResponseString = jsonDefaultObject.toString();

                                break;
                        }
                    }
                    else
                    {
                        JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                        jsonResponseString = jsonObject.toString();
                    }
                }
                else
                {
                    JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                    jsonResponseString = jsonObject.toString();
                }
            }
            else
            {
                JSONObject jsonObject = getTextResponseForName(fallbackMessage);

                jsonResponseString = jsonObject.toString();
            }
        }
        catch (ParseException e)
        {
            JSONObject jsonObject = getTextResponseForName(fallbackMessage);

            jsonResponseString = jsonObject.toString();
        }

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        outputStreamWriter.write(jsonResponseString);
        outputStreamWriter.close();
    }

    private String getResponseForNameWithCategoty(String name, String category)
    {
        try
        {
            URL urlDetail = new URL("https://www.abbreviations.com/services/v2/abbr.php?uid=6512&tokenid=VZxTuZEHrTX11uV5&term=" + name + "&categoryid=" + category);

            HttpsURLConnection connection = (HttpsURLConnection) urlDetail.openConnection();

            connection.setDoOutput(true);

            connection.setRequestMethod("GET");

            connection.setRequestProperty("Content-Type", "application/xml");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

            StringBuilder resultBuilder = new StringBuilder();

            String jsonOutput;

            while ((jsonOutput = bufferedReader.readLine()) != null)
            {
                resultBuilder.append(jsonOutput);
            }

            try
            {
                org.json.JSONObject jsonObject = XML.toJSONObject(resultBuilder.toString());

                return jsonObject.toString();
            }
            catch (JSONException e)
            {
                return "I could not find any abbreviation or acronym for \"" + name + "\" in category \"" + category + "\". Please say correct or another abbreviation or acronym name";
            }
        }
        catch (IOException e)
        {
            return "I could not find any abbreviation or acronym for \"" + name + "\" in category \"" + category + "\". Please say correct or another abbreviation or acronym name";
        }
    }

    private String getResponseForName(String name)
    {
        try
        {
            URL urlDetail = new URL("https://www.abbreviations.com/services/v2/abbr.php?uid=6512&tokenid=VZxTuZEHrTX11uV5&term=" + name);

            HttpsURLConnection connection = (HttpsURLConnection) urlDetail.openConnection();

            connection.setDoOutput(true);

            connection.setRequestMethod("GET");

            connection.setRequestProperty("Content-Type", "application/xml");

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

            StringBuilder resultBuilder = new StringBuilder();

            String jsonOutput;

            while ((jsonOutput = bufferedReader.readLine()) != null)
            {
                resultBuilder.append(jsonOutput);
            }

            try
            {
                org.json.JSONObject jsonObject = XML.toJSONObject(resultBuilder.toString());

                return jsonObject.toString();
            }
            catch (JSONException e)
            {
                return "I could not find any abbreviation or acronym for \"" + name + ". Please say correct or another abbreviation or acronym name";
            }
        }
        catch (IOException e)
        {
            return "I could not find any abbreviation or acronym for \"" + name + ". Please say correct or another abbreviation or acronym name";
        }
    }

    private JSONObject getTextResponseForName(String speechText)
    {
        JSONObject responseForName = new JSONObject();

        responseForName.put(Constant.fulfillmentText,speechText);

        JSONObject fulFillMessageObject = new JSONObject();

        JSONArray fulFillMessageArray = new JSONArray();

        JSONObject textResponseObject = new JSONObject();

        JSONArray textResponseArray = new JSONArray();

        textResponseArray.add(speechText);

        textResponseObject.put(Constant.text,textResponseArray);

        fulFillMessageObject.put(Constant.text,textResponseObject);

        fulFillMessageArray.add(fulFillMessageObject);

        responseForName.put(Constant.fulfillmentMessages,fulFillMessageArray);

        responseForName.put("source","");
        responseForName.put("payload", (Collection<?>) null);
        responseForName.put("outputContexts", (Collection<?>) null);
        responseForName.put("followupEventInput", (Collection<?>) null);

        return responseForName;
    }
}
