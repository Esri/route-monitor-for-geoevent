package com.esri.ges.processor.plan;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.manager.plan.PlanManager;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;

public class PlanService extends GeoEventProcessorServiceBase
{ 
  private PlanManager planManager;

  public PlanService()
  {
    definition = new PlanDefinition();
  }

  @Override
  public GeoEventProcessor create() throws ComponentException
  {
    return new PlanProcessor(definition, planManager);
  }
  
  public void setPlanManager(PlanManager planManager)
  {
    this.planManager = planManager;
  }
}
