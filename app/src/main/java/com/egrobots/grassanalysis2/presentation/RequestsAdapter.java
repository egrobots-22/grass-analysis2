package com.egrobots.grassanalysis2.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.egrobots.grassanalysis2.R;
import com.egrobots.grassanalysis2.models.Request;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    private List<Request> requestsList;


    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item_layout, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requestsList.get(position);
        holder.requestIdTextView.setText(request.getId());
        holder.progressStatusTextView.setText(request.getStatus());
    }

    @Override
    public int getItemCount() {
        return requestsList.size();
    }

    public void setItems(List<Request> requestList) {
        this.requestsList = requestList;
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.request_id_tv)
        TextView requestIdTextView;
        @BindView(R.id.progress_status_tv)
        TextView progressStatusTextView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
