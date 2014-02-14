package esri.mrm.mobile;

public enum NonServiceWorkOrderType
{
  Base, Break, Lunch;

  public static boolean contains(String test)
  {

    for (NonServiceWorkOrderType c : NonServiceWorkOrderType.values())
    {
      if (c.name().equals(test))
      {
        return true;
      }
    }

    return false;
  }
}
