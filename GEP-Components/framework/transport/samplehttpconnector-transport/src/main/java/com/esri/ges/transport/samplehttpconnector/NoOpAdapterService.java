package com.esri.ges.transport.samplehttpconnector;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.adapter.Adapter;
import com.esri.ges.adapter.AdapterServiceBase;
import com.esri.ges.adapter.AdapterType;
import com.esri.ges.core.component.ComponentException;

public class NoOpAdapterService extends AdapterServiceBase
{
	final private static Log log = LogFactory.getLog( NoOpAdapterService.class );
	public NoOpAdapterService()
	{
		super();
		NoOpAdapterDefinition noopDefinition = new NoOpAdapterDefinition( AdapterType.INBOUND );
		definition = noopDefinition;
		loadConnectorFromStream(noopDefinition, "samplehttp-inbound-connector.xml");
	}
	
	private void loadConnectorFromStream( NoOpAdapterDefinition awsCanalDefinition, String resource )
	{
		try
		{
			awsCanalDefinition.loadConnector( getResourceAsStream( resource ) );
		}
		catch (JAXBException e)
		{
			log.error( e.getMessage(), e );
		}
	}

	@Override
	public Adapter createAdapter() throws ComponentException
	{
		return null;
	}
}
