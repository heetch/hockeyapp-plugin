
package de.felixschulze.gradle

import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.protocol.HTTP
import org.apache.tools.ant.taskdefs.condition.Http
import org.apache.http.client.HttpClient

import java.nio.charset.Charset

public class JiraTask {

    static def String pasteBuildUrlToJiraCard(HttpClient httpClient, HockeyAppPluginExtension hockeyApp, String hockeyAppPublicUrl, String hockeyAppConfigUrl) {
        def buildNumberUrl = hockeyAppPublicUrl + extractBuildNumberPath(hockeyAppConfigUrl)

        if (hockeyApp.jiraUrlTitle != null &&
                hockeyApp.jiraRepoUrl != null && hockeyApp.jiraCard != null
                && hockeyApp.jiraPassword != null && hockeyApp.jiraUsername != null) {
            String credentials = hockeyApp.jiraUsername + ":" + hockeyApp.jiraPassword;
            String data = '{"object": {"url":"' + buildNumberUrl + '", "title":"' + hockeyApp.jiraUrlTitle + '"}}'

            HttpPost httpPost = new HttpPost("https://${hockeyApp.jiraRepoUrl}/rest/api/2/issue/${hockeyApp.jiraCard}/remotelink")

            String base64EncodedCredentials = credentials.bytes.encodeBase64().toString()
            httpPost.setHeader("Authorization", "Basic " + base64EncodedCredentials);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");

            httpPost.setEntity(new ByteArrayEntity(data.getBytes(Charset.forName("UTF-8"))))
            HttpResponse response = httpClient.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                return "Error " + response.getEntity().getContent()
            } else {
                return "Success, build url " + buildNumberUrl
            }
        }
        return null
    }

    // It sucks but that's the only place in the hockeyapp payload where the build number is given
    def static String extractBuildNumberPath(String hockeyAppConfigUrl) {
        return hockeyAppConfigUrl?.substring(hockeyAppConfigUrl?.lastIndexOf("/app_versions"), hockeyAppConfigUrl?.length())
    }
}