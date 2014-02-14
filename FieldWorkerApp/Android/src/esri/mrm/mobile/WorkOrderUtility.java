package esri.mrm.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkOrderUtility
{
  public static List<WorkOrder> getWorkOrderListForBreaks(List<WorkOrder> allWorkOrders)
  {
    // Business rules: the new list should contain the last completed or
    // exception stop, future stops sorted by sequence number and less the last
    // stop.
    // But if there is an atStop stop, this should be the first stop in the list
    // followed by future stops.
    // Also cannot insert a break after another.
    List<WorkOrder> newList = new ArrayList<WorkOrder>();
    List<WorkOrder> completedList = new ArrayList<WorkOrder>();
    List<WorkOrder> futureList = new ArrayList<WorkOrder>();
    //for (WorkOrder wo : allWorkOrders)
    for(Iterator<WorkOrder> it = allWorkOrders.iterator(); it.hasNext(); )
    {
      WorkOrder wo = it.next();
      if (wo.getStatus().equals(WorkOrderStatus.Completed.toString()) || wo.getStatus().equals(WorkOrderStatus.Exception.toString()))
        completedList.add(wo);
      else if (wo.getStatus().equals(WorkOrderStatus.AtStop.toString()))
        newList.add(wo);
      else if (!wo.getType().equals(NonServiceWorkOrderType.Break.toString()))
        futureList.add(wo);
    }

    if (completedList.size() > 0 && newList.size() == 0)
    {
      if (completedList.size() > 1)
      {
        try
        {
          Collections.sort(completedList, new actualDepartureComparator());
        }
        catch(Exception e)
        {
          Collections.sort(completedList, new sequenceComparator());
        }
      }
      newList.add(completedList.get(completedList.size() - 1));
    }

    // If the last one is a break, do not include in the list.
    if (newList.size() > 0)
    {
      if (newList.get(0).getType().equals(NonServiceWorkOrderType.Break.toString()))
        newList.clear();
    }

    if (futureList.size() > 1)
    {
      Collections.sort(futureList, new sequenceComparator());
      // skip the last stop
      for (int i = 0; i < futureList.size() - 1; i++)
      {
        newList.add(futureList.get(i));
      }
    }
    return newList;
  }
  
  public static List<WorkOrder> getWorkOrderListForWorkOrder(List<WorkOrder> allWorkOrders, WorkOrder workOrder)
  {
    // Business rules: 
    // - the new list should contain the last completed or exception stop,   
    // - future stops sorted by sequence number and less the last stop if it is base or break
    // - if there is an atStop stop, this should be the first stop in the list followed by future stops.
    // - the list should not contain itself.
    List<WorkOrder> newList = new ArrayList<WorkOrder>();
    List<WorkOrder> completedList = new ArrayList<WorkOrder>();
    List<WorkOrder> futureList = new ArrayList<WorkOrder>();
    //for (WorkOrder wo : allWorkOrders)
    for(Iterator<WorkOrder> it = allWorkOrders.iterator(); it.hasNext(); )
    {
      WorkOrder wo = it.next();
      if (wo.getStatus().equals(WorkOrderStatus.Completed.toString()) || wo.getStatus().equals(WorkOrderStatus.Exception.toString()))
        completedList.add(wo);
      else if (wo.getStatus().equals(WorkOrderStatus.AtStop.toString()))
        newList.add(wo);
      else if ( ! wo.getStopName().equals(workOrder.getStopName()))
        futureList.add(wo);
    }

    //  Adding the last completed stop to the list. Or, if the list already contains a atStop stop, won't need completed stops
    if (completedList.size() > 0 && newList.size() == 0)
    {
      if (completedList.size() > 1)
      {
        try
        {
          Collections.sort(completedList, new actualDepartureComparator());
        }
        catch(Exception e)
        {
          Collections.sort(completedList, new sequenceComparator());
        }
      }
      newList.add(completedList.get(completedList.size() - 1));
    }

    if (futureList.size() > 1)
    {
      Collections.sort(futureList, new sequenceComparator());
      for (int i = 0; i < futureList.size(); i++)
      {
        if (i == futureList.size() - 1)
        {
          if (!NonServiceWorkOrderType.contains(futureList.get(i).getType()))
          {
            newList.add(futureList.get(i));
          }
        }
        else
          newList.add(futureList.get(i));
      }
    }
    return newList;
  }
  

  private static class actualDepartureComparator implements Comparator<WorkOrder>
  {
    public int compare(WorkOrder lhs, WorkOrder rhs)
    {
      return lhs.getActualDepartureAsLong().compareTo(rhs.getActualDepartureAsLong());
    }
  }

  private static class sequenceComparator implements Comparator<WorkOrder>
  {
    public int compare(WorkOrder lhs, WorkOrder rhs)
    {
      return (new Integer(lhs.getSequence())).compareTo(new Integer(rhs.getSequence()));
    }
  }

  public static Map sortByValue(Map map) {
    List list = new LinkedList(map.entrySet());
    Collections.sort(list, new Comparator() {
         public int compare(Object o1, Object o2) {
              return ((Comparable) ((Map.Entry) (o1)).getValue())
             .compareTo(((Map.Entry) (o2)).getValue());
         }
    });

   Map result = new LinkedHashMap();
   for (Iterator it = list.iterator(); it.hasNext();) {
       Map.Entry entry = (Map.Entry)it.next();
       result.put(entry.getKey(), entry.getValue());
   }
   return result;
  } 
  
  
}
