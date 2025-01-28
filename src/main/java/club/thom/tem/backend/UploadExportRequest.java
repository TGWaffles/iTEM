package club.thom.tem.backend;

import club.thom.tem.models.RequestData;
import club.thom.tem.models.messages.ClientMessages;
import club.thom.tem.storage.TEMConfig;
import club.thom.tem.util.MessageUtil;
import club.thom.tem.util.RequestUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

public class UploadExportRequest {
    private static final Logger logger = LogManager.getLogger(UploadExportRequest.class);
    TEMConfig config;
    URL backendUrl;
    ClientMessages.ClientMessage messageToSend;
    boolean sendMessages = true;

    public UploadExportRequest(TEMConfig config, ClientMessages.ClientMessage messageToSend, URL backendUrl) {
        this.config = config;
        this.messageToSend = messageToSend;
        this.backendUrl = backendUrl;
    }

    public void setSendMessages(boolean sendMessages) {
        this.sendMessages = sendMessages;
    }

    /**
     * @return The status code of the response.
     */
    public int send() {
        RequestData response = submitRequest();
        if (response.getStatus() >= 400) {
            if (sendMessages) {
                MessageUtil.tellPlayerAboutFailedRequest(response.getStatus());
            } else {
                logger.error("TEM returned error when uploading export: " + response.getStatus());
            }
            return response.getStatus();
        }

        return response.getStatus();
    }

    public RequestData submitRequest() {
        String urlAsString = backendUrl.toString();
        if (backendUrl.getHost().equalsIgnoreCase("api.tem.cx")) {
            urlAsString += "?key=" + config.getTemApiKey();
        }
        return new RequestUtil().sendPostRequest(urlAsString, messageToSend.toByteArray());
    }
}
