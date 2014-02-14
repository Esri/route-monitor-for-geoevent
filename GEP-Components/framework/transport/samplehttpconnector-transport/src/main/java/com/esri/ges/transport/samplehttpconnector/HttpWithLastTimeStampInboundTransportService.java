package com.esri.ges.transport.samplehttpconnector;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.transport.Transport;
import com.esri.ges.transport.http.HttpInboundTransportService;
import com.esri.ges.transport.util.XmlTransportDefinition;

public class HttpWithLastTimeStampInboundTransportService extends HttpInboundTransportService
{

  public HttpWithLastTimeStampInboundTransportService()
  {
    super();
    definition = new XmlTransportDefinition(getResourceAsStream("samplehttp-inbound-transport-definition.xml"),
        super.definition);
  }

  @Override
  public Transport createTransport() throws ComponentException
  {
    return new HttpWithLastTimeStampInboundTransport(definition);
  }
}
