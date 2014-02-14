using FieldWorker.Common;
using FieldWorker.Schema;
using System.Windows.Media.Imaging;

namespace FieldWorker.ViewModels
{
  class MessagesViewModel : BaseItemsViewModel
  {
    /*
    public MessagesViewModel() :
      base(null)
    {
      Init();
    }
    */

    public MessagesViewModel(FeatureLayerHelper featureLayerHelper) :
      base(featureLayerHelper)
    {
      Init();
    }

    private void Init()
    {
      TrackIdFieldName = "OBJECTID";
      GroupByFieldName = null;
      SortByFieldName1 = Message.TimeFieldAlias;
      SortByFieldName2 = null;
      SortByFieldOrder1 = System.ComponentModel.ListSortDirection.Descending;
      SortByFieldOrder2 = System.ComponentModel.ListSortDirection.Ascending;
    }

    override protected Item CreateItem()
    {
      Item item = new Message(this);
      return item;
    }

    public BitmapSource InternalResourceTabImageSource
    {
      get
      {
        string imagePath = HasNewMessages() ? "/Images/TabMessagesNew.png" : "/Images/TabMessages.png";
        return ImagesCache.Instance.Get(imagePath, true);
      }
    }

    private bool HasNewMessages()
    {
      foreach (Message msg in Items)
      {
        if (msg.IsNewMessage())
          return true;
      }

      return false;
    }
  }
}
