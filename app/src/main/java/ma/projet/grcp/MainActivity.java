package ma.projet.grcp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.ArrayAdapter;

import ma.projet.grpc.stubs.TypeCompte;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import androidx.appcompat.app.AlertDialog;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ma.projet.grpc.stubs.Compte;
import ma.projet.grpc.stubs.CompteServiceGrpc;
import ma.projet.grpc.stubs.GetAllComptesRequest;
import ma.projet.grpc.stubs.GetAllComptesResponse;
import ma.projet.grpc.stubs.SaveCompteRequest;
import ma.projet.grpc.stubs.SaveCompteResponse;
import ma.projet.grpc.stubs.CompteRequest;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView viewAccountsList;
    private CompteAdapter accountAdapter;
    private List<Compte> accountCollection = new ArrayList<>();
    private Button actionButtonCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUserInterface();
        fetchAccountsFromServer();
    }

    private void initializeUserInterface() {
        viewAccountsList = findViewById(R.id.recyclerViewComptes);
        actionButtonCreateAccount = findViewById(R.id.ajouterCompte);

        accountAdapter = new CompteAdapter(accountCollection);
        viewAccountsList.setLayoutManager(new LinearLayoutManager(this));
        viewAccountsList.setAdapter(accountAdapter);

        actionButtonCreateAccount.setOnClickListener(v -> launchAccountCreationDialog());
    }

    private void fetchAccountsFromServer() {
        new Thread(() -> retrieveServerAccounts()).start();
    }

    private void retrieveServerAccounts() {
        ManagedChannel connectionChannel = ManagedChannelBuilder
                .forAddress("10.0.2.2", 9090)
                .usePlaintext()
                .build();

        try {
            CompteServiceGrpc.CompteServiceBlockingStub serverStub = CompteServiceGrpc.newBlockingStub(connectionChannel);
            GetAllComptesRequest serverRequest = GetAllComptesRequest.newBuilder().build();
            GetAllComptesResponse serverResponse = serverStub.allComptes(serverRequest);

            runOnUiThread(() -> {
                accountCollection.clear();
                accountCollection.addAll(serverResponse.getComptesList());
                accountAdapter.notifyDataSetChanged();
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la communication avec le serveur", e);
        } finally {
            connectionChannel.shutdown();
        }
    }

    private void launchAccountCreationDialog() {
        View dialogContentView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);

        EditText inputAccountBalance = dialogContentView.findViewById(R.id.modifierSolde);
        Spinner selectAccountType = dialogContentView.findViewById(R.id.spinner);
        Button confirmAccountCreation = dialogContentView.findViewById(R.id.saveCompte);

        ArrayAdapter<String> typeSelectionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"COURANT", "EPARGNE"});
        typeSelectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectAccountType.setAdapter(typeSelectionAdapter);

        AlertDialog creationDialog = new AlertDialog.Builder(this)
                .setTitle("Saisissez votre montant")
                .setView(dialogContentView)
                .create();

        confirmAccountCreation.setOnClickListener(v -> processAccountCreation(inputAccountBalance, selectAccountType, creationDialog));

        creationDialog.show();
    }

    private void processAccountCreation(EditText balanceInput, Spinner typeSelector, AlertDialog dialog) {
        String balanceText = balanceInput.getText().toString().trim();

        if (!balanceText.isEmpty()) {
            double accountBalance = Double.parseDouble(balanceText);

            new Thread(() -> sendAccountToServer(accountBalance, typeSelector.getSelectedItem().toString(), dialog)).start();
        }
    }

    private void sendAccountToServer(double balance, String accountType, AlertDialog dialog) {
        ManagedChannel serverConnection = ManagedChannelBuilder
                .forAddress("10.0.2.2", 9090)
                .usePlaintext()
                .build();

        try {
            CompteServiceGrpc.CompteServiceBlockingStub serverStub = CompteServiceGrpc.newBlockingStub(serverConnection);

            SaveCompteRequest creationRequest = SaveCompteRequest.newBuilder()
                    .setCompte(
                            CompteRequest.newBuilder()
                                    .setSolde((float) balance)
                                    .setDateCreation(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                                    .setType(TypeCompte.valueOf(accountType))
                                    .build()
                    )
                    .build();

            SaveCompteResponse serverResponse = serverStub.saveCompte(creationRequest);

            GetAllComptesRequest refreshRequest = GetAllComptesRequest.newBuilder().build();
            GetAllComptesResponse refreshResponse = serverStub.allComptes(refreshRequest);

            runOnUiThread(() -> {
                accountCollection.clear();
                accountCollection.addAll(refreshResponse.getComptesList());
                accountAdapter.notifyDataSetChanged();
                dialog.dismiss();
            });

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'ajout du compte", e);
            runOnUiThread(() -> {
                // Espace réservé pour un éventuel message d'erreur utilisateur
            });
        } finally {
            serverConnection.shutdown();
        }
    }
}