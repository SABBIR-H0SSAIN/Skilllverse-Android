package com.example.skillverse_android.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.models.Certificate;
import com.google.android.material.button.MaterialButton;
import java.util.List;
public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {
    private List<Certificate> certificates;
    private OnCertificateClickListener listener;
    public interface OnCertificateClickListener {
        void onDownloadClick(Certificate certificate);
    }
    public CertificateAdapter(List<Certificate> certificates, OnCertificateClickListener listener) {
        this.certificates = certificates;
        this.listener = listener;
    }
    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_certificate, parent, false);
        return new CertificateViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        Certificate certificate = certificates.get(position);
        holder.bind(certificate);
    }
    @Override
    public int getItemCount() {
        return certificates.size();
    }
    class CertificateViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvStudentName, tvDateCompleted, tvScore, tvCertificateId;
        MaterialButton btnDownload;
        CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvDateCompleted = itemView.findViewById(R.id.tvDateCompleted);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvCertificateId = itemView.findViewById(R.id.tvCertificateId);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
        void bind(Certificate certificate) {
            tvCourseName.setText(certificate.getCourseName());
            tvStudentName.setText(certificate.getStudentName());
            tvDateCompleted.setText(certificate.getDateCompleted());
            tvScore.setText(String.format("%d%%", (int)certificate.getScore()));
            tvCertificateId.setText("Certificate ID: " + certificate.getCertificateId());
            btnDownload.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownloadClick(certificate);
                }
            });
        }
    }
}
