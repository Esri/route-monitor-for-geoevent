package esri.mrm.mobile.adapter;

import esri.mrm.mobile.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AllAttributesAdatper extends BaseAdapter
{

  Context mContext;
  private String [] id = {"S001","S002","S003","S004","S005","S006","S007"};
  private String [] name={"Rohit","Rahul","Ravi","Amit","Arun","Anil","Kashif"};
  private LayoutInflater mInflater;
  
  public AllAttributesAdatper(Context c)
  {
         mContext=c;
         mInflater = LayoutInflater.from(c);
  }
  public int getCount()
  {
         return id.length;
  }
  public Object getItem(int position)
  {
         return position;
  }
  public long getItemId(int position)
  {
         return position;
  }
  public View getView(int position, View convertView, ViewGroup parent)
  {
         ViewHolder holder=null;
         if(convertView==null)
         {
                convertView = mInflater.inflate(R.layout.customgrid, 
                                                               parent,false);
                holder = new ViewHolder();
                holder.txtId=(TextView)convertView.findViewById(R.id.attribute_name);
                holder.txtId.setPadding(100, 10,10 , 10);
                holder.txtName=(TextView)convertView.findViewById(R.id.attribute_value);
                holder.txtName.setPadding(100, 10, 10, 10);
                if(position==0)
                {                             
                      convertView.setTag(holder);
                }
         }
         else
         {
                holder = (ViewHolder) convertView.getTag();
         }
         holder.txtId.setText(id[position]);
         holder.txtName.setText(name[position]);
         return convertView;
  }
  
  static class ViewHolder
  {        
         TextView txtId;        
         TextView txtName;               
  }

}
