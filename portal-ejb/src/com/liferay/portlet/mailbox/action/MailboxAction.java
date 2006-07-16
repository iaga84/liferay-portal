/**
 * Copyright (c) 2000-2006 Liferay, LLC. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portlet.mailbox.action;

import java.util.List;

import javax.mail.Folder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.json.JSONArray;
import org.json.JSONObject;

import com.liferay.portal.struts.JSONAction;
import com.liferay.portal.util.Constants;
import com.liferay.portal.util.WebKeys;
import com.liferay.portlet.mailbox.util.MailEnvelope;
import com.liferay.portlet.mailbox.util.MailMessage;
import com.liferay.portlet.mailbox.util.MailUtil;
import com.liferay.util.ParamUtil;

/**
 * <a href="MailboxAction.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Ming-Gih Lam
 *
 */
public class MailboxAction extends JSONAction {

	public String getJSON(
			ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res)
		throws Exception {
		
		String cmd = ParamUtil.getString(req, Constants.CMD);
		String userId = ParamUtil.getString(req, "userId");
		String rtString = "";
		
		req.getSession().setAttribute(WebKeys.USER_ID, userId);
		req.getSession().setAttribute(WebKeys.USER_PASSWORD, "liferaymailx");

		if ("getFolders".equals(cmd)) {
			List folders = MailUtil.getAllFolders(MailUtil.getStore(req));
			
			rtString = _getFolders(folders);
		}
		else if ("getPreview".equals(cmd)) {
			String folderId = ParamUtil.getString(req, "folderId");
			Folder folder = MailUtil.getFolder(req, folderId);
			
			rtString = _getPreviewHeaders(userId, folder);
		}
		else if ("getMessage".equals(cmd)) {
			Folder folder = MailUtil.getFolder(req, "Inbox");
			int messageId = ParamUtil.getInteger(req, "messageId");
			
			rtString = _getMessage(folder, messageId);
		}
			
		MailUtil.closeStore(req);
		
		System.out.println("super");
		
		return rtString;
	}
	
	private String _getFolders(List folders) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jFolders = new JSONArray();
		
		try {
			for (int i = 0; i < folders.size(); i++) {
				Folder folderObj = (Folder)folders.get(i);
				JSONObject jFolderObj = new JSONObject();;
				
				jFolderObj.put("name", folderObj.getName());
				jFolderObj.put("id", folderObj.getName());
				jFolderObj.put("newCount", folderObj.getNewMessageCount());
				jFolderObj.put("totalCount", folderObj.getMessageCount());
				
				jFolders.put(jFolderObj);
			}
		}
		catch (Exception e) {
		}
		
		jsonObj.put("folders", jFolders);
		
		return jsonObj.toString();
	}
	
	private String _getMessage(Folder folder, int messageId) {
		JSONObject jsonObj = new JSONObject();
		
		try {
			MailMessage mm = MailUtil.getMessage(folder, messageId);
			jsonObj.put("body", mm.getHtmlBody());
		}
		catch (Exception e) {
		}
		
		return jsonObj.toString();
	}
	
	private String _getPreviewHeaders(String userId, Folder folder) {
		JSONObject jsonObj = new JSONObject();
		JSONArray meArray = new JSONArray();
		try {
			List list = MailUtil.getEnvelopes(folder);
			
			for (int i = 0; i < list.size(); i++) {
				MailEnvelope me = (MailEnvelope)list.get(i);
				JSONObject jMe = new JSONObject();
				
				jMe.put("id", me.getUID());
				jMe.put("email", me.getEmail());
				jMe.put("subject", me.getSubject());
				jMe.put("date", me.getDate().toString());
				meArray.put(jMe);
			}
			
			jsonObj.put("headers", meArray);
		}
		catch (Exception e) {
		}
		
		return jsonObj.toString();
	}

}