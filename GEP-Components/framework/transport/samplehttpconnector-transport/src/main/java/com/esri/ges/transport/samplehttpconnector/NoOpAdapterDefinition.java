package com.esri.ges.transport.samplehttpconnector;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import com.esri.ges.adapter.AdapterDefinitionBase;
import com.esri.ges.adapter.AdapterType;
import com.esri.ges.jaxb.connector.ConnectorWrapper;

public class NoOpAdapterDefinition extends AdapterDefinitionBase
{

	public NoOpAdapterDefinition( AdapterType type )
	{
		super(type);
		
	}
	
	public void loadConnector( InputStream is ) throws JAXBException
	{
		Unmarshaller unmarshaller = JAXBContext.newInstance(ConnectorWrapper.class).createUnmarshaller();
		unmarshaller.setEventHandler(new DefaultValidationEventHandler());
		Object obj = unmarshaller.unmarshal(is);
		ConnectorWrapper connectorWrapper = (ConnectorWrapper) obj;
		getConnectors().add(connectorWrapper.convert());
	}

	@Override
	public String getName()
	{
		return "SampleHTTP NoOp Adapter (DO NOT USE)";
	}
	
	

}
