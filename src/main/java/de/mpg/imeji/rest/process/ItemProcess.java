package de.mpg.imeji.rest.process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import de.mpg.imeji.logic.auth.Authentication;
import de.mpg.imeji.logic.auth.AuthenticationFactory;
import de.mpg.imeji.logic.auth.exception.NotAllowedError;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.rest.api.ItemService;
import de.mpg.imeji.rest.to.ItemTO;
import de.mpg.imeji.rest.to.ItemWithFileTO;
import de.mpg.imeji.rest.to.JSONResponse;
import de.mpg.j2j.exceptions.NotFoundException;

public class ItemProcess {
	
	public static JSONResponse deleteItem(HttpServletRequest req, String id) {
		User u = BasicAuthentication.auth(req);
		JSONResponse resp = new JSONResponse();
		
		if (u == null) {
			resp.setObject(RestProcessUtils.buildUnauthorizedResponse("Not logged in not allowed to delete item"));
			resp.setStatus(Status.UNAUTHORIZED);
		}
		else {
			ItemService icrud = new ItemService();
			try {
				icrud.delete(id, u);
				resp.setStatus(Status.OK);
			} catch (NotFoundException e) {
				resp.setObject(RestProcessUtils.buildBadRequestResponse(e.getLocalizedMessage()));
				resp.setStatus(Status.BAD_REQUEST);
	
			} catch (NotAllowedError e) {
	
					resp.setObject(RestProcessUtils.buildNotAllowedResponse(e.getLocalizedMessage()));
					resp.setStatus(Status.FORBIDDEN);
			}
		}
		return resp;
	}
	

	public static JSONResponse readItem(HttpServletRequest req, String id) {
		User u = BasicAuthentication.auth(req);
		JSONResponse resp = new JSONResponse();

		ItemTO item = null;

		ItemService icrud = new ItemService();
		try {
			item = icrud.read(id, u);
			resp.setObject(item);
			resp.setStatus(Status.OK);
		} catch (NotFoundException e) {
			resp.setObject(RestProcessUtils.buildBadRequestResponse(e.getLocalizedMessage()));
			resp.setStatus(Status.BAD_REQUEST);

		} catch (NotAllowedError e) {
			if (u == null) {
				resp.setObject(RestProcessUtils.buildUnauthorizedResponse(e.getLocalizedMessage()));
				resp.setStatus(Status.UNAUTHORIZED);
			} else {
				resp.setObject(RestProcessUtils.buildNotAllowedResponse(e.getLocalizedMessage()));
				resp.setStatus(Status.FORBIDDEN);
			}
		} catch (Exception e) {
			resp.setObject(RestProcessUtils.buildExceptionResponse(e.getLocalizedMessage()));
			resp.setStatus(Status.FORBIDDEN);
		}
		return resp;

	}

	public static JSONResponse createItem(HttpServletRequest req, InputStream file, String json, String origName) {
		
		// Load User (if provided)
		User u = BasicAuthentication.auth(req);
		
		// Parse json into to
		ItemWithFileTO to = (ItemWithFileTO) RestProcessUtils.buildTOFromJSON(req, ItemWithFileTO.class, json);
		// set file in to (if provided)
		
		if (file != null) {
			try {
				File tmp = File.createTempFile("imejiAPI", null);
				IOUtils.copy(file, new FileOutputStream(tmp));
				to.setFile(tmp);
				if(to.getFilename() == null || "".equals(to.getFilename()))
					to.setFilename(origName);
				else
					to.setFilename(to.getFilename() + "." + FilenameUtils.getExtension(origName));
					
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// create item with the file
		ItemService service = new ItemService();

		// / write response
		JSONResponse resp = new JSONResponse();
		try {
			resp.setObject(service.create(to, u));
			resp.setStatus(Status.CREATED);
		} catch (NotFoundException e) {
			resp.setObject(RestProcessUtils.buildBadRequestResponse(e.getLocalizedMessage()));
			resp.setStatus(Status.BAD_REQUEST);

		} catch (NotAllowedError e) {
			if (u == null) {
				resp.setObject(RestProcessUtils.buildUnauthorizedResponse(e.getLocalizedMessage()));
				resp.setStatus(Status.UNAUTHORIZED);
			} else {
				resp.setObject(RestProcessUtils.buildNotAllowedResponse(e.getLocalizedMessage()));
				resp.setStatus(Status.FORBIDDEN);
			}
		} catch (Exception e) {
			resp.setStatus(Status.INTERNAL_SERVER_ERROR);
		}

		return resp;
	}

}
