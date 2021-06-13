package gr.aueb.distributedsystems.tikatok.activities.fragmentTopics;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import gr.aueb.distributedsystems.tikatok.R;
import java.util.List;

public class StringTopicRecyclerViewAdapter extends RecyclerView.Adapter<StringTopicRecyclerViewAdapter.ViewHolder> {

    private final List<String> mValues;
    private StringTopicFragment.OnFragmentInteractionListener listener;

    public StringTopicRecyclerViewAdapter(List<String> items, StringTopicFragment.OnFragmentInteractionListener listener) {
        mValues = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_string_topic, parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String topic = mValues.get(position);
        holder.mItem = topic;
        holder.txtStringTopic.setText(topic);
        holder.btnViewStringTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onView(topic);
            }
        });
        holder.btnSubscribeStringTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSubscribe(topic);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView txtStringTopic;
        public final Button btnViewStringTopic;
        public final Button btnSubscribeStringTopic;
        public String mItem;
        public final View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            txtStringTopic = view.findViewById(R.id.txtStringTopic);
            btnViewStringTopic = view.findViewById(R.id.btnViewStringTopic);
            btnSubscribeStringTopic = view.findViewById(R.id.btnSubscribeStringTopic);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + txtStringTopic.getText() + "'";
        }
    }
}