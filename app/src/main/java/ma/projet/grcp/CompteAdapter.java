package ma.projet.grcp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ma.projet.grpc.stubs.Compte;

public class CompteAdapter extends RecyclerView.Adapter<CompteAdapter.CompteViewHolder> {

    private List<Compte> comptes;

    public CompteAdapter(List<Compte> comptes) {
        this.comptes = comptes;
    }

    @NonNull
    @Override
    public CompteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_compte, parent, false);
        return new CompteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompteViewHolder holder, int position) {
        Compte compte = comptes.get(position);
        holder.tvCompteId.setText("ID: " + compte.getId());
        holder.tvSolde.setText("Solde: " + compte.getSolde());
        holder.tvDateCreation.setText("Date: " + compte.getDateCreation());
        holder.tvType.setText("Type: " + compte.getType());
    }

    @Override
    public int getItemCount() {
        return comptes.size();
    }

    static class CompteViewHolder extends RecyclerView.ViewHolder {
        TextView tvCompteId, tvSolde, tvDateCreation, tvType;

        public CompteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCompteId = itemView.findViewById(R.id.id);
            tvSolde = itemView.findViewById(R.id.solde);
            tvDateCreation = itemView.findViewById(R.id.date);
            tvType = itemView.findViewById(R.id.typeCompte);
        }
    }
}
