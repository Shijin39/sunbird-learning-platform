package com.ilimi.taxonomy.content.validator;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.AssetsMimeTypeMap;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.util.PropertiesUtil;

public class ContentValidator {
	
	public boolean isValidContentPackage(File file) {
		boolean isValidContentPackage = false;
		try {
			if (file.exists()) {
				if (!isValidContentMimeType(file))
					throw new ClientException(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_FILE_MIME_TYPE_ERROR, "The uploaded package is invalid.");
				if (!isValidContentPackageStructure(file))
					throw new ClientException(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR, "The uploaded package has invalid folder structure.");
				if (!isValidContentSize(file))
					throw new ClientException(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_SIZE_ERROR, "The uploaded package has exceeds the file package size limit.");
				
				isValidContentPackage = true;
			}
		} catch(IOException e) {
			throw new ClientException(ContentErrorMessageConstants.CONTENT_PACKAGE_FILE_OPERATION_ERROR, "Something went wrong while processing the Package file.");
		} catch(Exception e) {
			throw new ClientException(ContentErrorMessageConstants.CONTENT_PACKAGE_VALIDATOR_ERROR, "Something went wrong while validating the Package file.");
		}
		return isValidContentPackage;
	}
	
	private boolean isValidContentMimeType(File file) throws IOException {
		boolean isValidMimeType = false;
		if (file.exists()) {
			Tika tika = new Tika();
			String mimeType = tika.detect(file);
			isValidMimeType = AssetsMimeTypeMap.isAllowedMimeType(mimeType);
		}
		return isValidMimeType;
	}
	
	private boolean isValidContentSize(File file) {
		boolean isValidSize = false;
		if (file.exists()) {
			if (file.length() < getContentPackageFileSizeLimit())
				isValidSize = true;
		}
		return isValidSize;
	}
	
	private double getContentPackageFileSizeLimit() {
		double size = 20971520;			// In Bytes, Default is 20MB
		String limit = PropertiesUtil.getProperty(ContentWorkflowPipelineParams.MAX_CONTENT_PACKAGE_FILE_SIZE_LIMIT.name());
		if (!StringUtils.isBlank(limit))
			size = Double.parseDouble(limit);
		return size;
	}
	
	@SuppressWarnings("resource")
	private boolean isValidContentPackageStructure(File file) throws IOException {
		final String JSON_ECML_FILE_NAME = "index.json";
		final String XML_ECML_FILE_NAME = "index.ecml";
		boolean isValidPackage = false;
		ZipFile zipFile = new ZipFile(file);
	    Enumeration<? extends ZipEntry> entries = zipFile.entries();
	    while(entries.hasMoreElements()){
	        ZipEntry entry = entries.nextElement();
	        if (StringUtils.equalsIgnoreCase(entry.getName(), JSON_ECML_FILE_NAME) || 
	        		StringUtils.equalsIgnoreCase(entry.getName(), XML_ECML_FILE_NAME)) {
	        	isValidPackage = true;
	        	break;
	        }
	    }
		return isValidPackage;
	}

}
