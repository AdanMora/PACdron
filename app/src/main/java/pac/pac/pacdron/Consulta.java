package pac.pac.pacdron;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import io.fabric.sdk.android.Fabric;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Consulta extends AppCompatActivity {

    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Class<?> mClss;

    Button btn_Consulta;
    TextView nombre;
    TextView provincia;
    TextView canton;
    TextView distrito;
    TextView centro_votacion;
    TextView direccion;
    TextView numMesa;
    TextView numElector;
    EditText inputCedula;
    ImageButton scanner_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_consulta);

        btn_Consulta = (Button) findViewById(R.id.btn_Consultar);
        scanner_btn = (ImageButton) findViewById(R.id.btn_camara);

        nombre = (TextView) findViewById(R.id.txt_nombre);
        provincia = (TextView) findViewById(R.id.txt_provincia);
        canton = (TextView) findViewById(R.id.txt_canton);
        distrito = (TextView) findViewById(R.id.txt_distrito);
        centro_votacion = (TextView) findViewById(R.id.txt_centro);
        direccion = (TextView) findViewById(R.id.txt_direccion);
        numMesa = (TextView) findViewById(R.id.txt_numMesa);
        numElector = (TextView) findViewById(R.id.txt_numElector);

        inputCedula = (EditText) findViewById(R.id.et_cedula);


        btn_Consulta.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                String cedStr = inputCedula.getText().toString();

                InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                v.setEnabled(false);

                if (cedStr.isEmpty() || cedStr.length() != 9){
                    Toast toast = Toast.makeText(getApplicationContext(), "Digite un número de cédula válido.", Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    Answers.getInstance().logContentView(new ContentViewEvent()
                            .putContentName("Consulta")
                            .putContentType("Consulta")
                            .putContentId("consulta"));

                    ProgressDialog progress = ProgressDialog.show(Consulta.this, "",
                            "Cargando consulta. Por favor espere...", true,false);

                    ConsultarCedula consulta = new ConsultarCedula();
                    consulta.execute(cedStr);

                    progress.dismiss();
                }

                v.setEnabled(true);
                inputCedula.setText("");

            }
        });

        final Activity activity = this;

        scanner_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(new ArrayList<>(Arrays.asList("PDF_417", "QR_CODE")));
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(true);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

    }

    /*public void launchScanner(View v) {
        launchActivity(Scanner.class);
    }

    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mClss != null) {
                        Intent intent = new Intent(this, mClss);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }*/

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "Resultado: " + result.getContents() + "  Formato: " + result.getFormatName(),Toast.LENGTH_LONG).show();
                System.out.println(result.getContents());
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private class ConsultarCedula extends AsyncTask<String, Void, JSONObject> {


        @Override
        protected JSONObject doInBackground(String... cedula) {

            HttpURLConnection connection = null;
            StringBuilder result = new StringBuilder();

            try {
                System.out.println(cedula[0]);
                JSONObject ced = new JSONObject();
                ced.put("numeroCedula",cedula[0]);

                URL url = new URL("http://www.tse.go.cr/dondevotarp/prRemoto.aspx/ObtenerDondeVotar");

                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(ced.toString().getBytes("UTF-8"));
                os.close();

                connection.connect();



                InputStream inputStream = connection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                return new JSONObject(result.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                connection.disconnect();
            }
            return null;

        }

        @Override
        protected void onPostExecute(JSONObject s) {
            JSONObject resultadoConsulta = null;
            Iterator keys = null;
            try {
                resultadoConsulta = s.getJSONObject("d").getJSONObject("lista");
                keys = resultadoConsulta.keys();

                if (!keys.hasNext()){
                    Toast toast = Toast.makeText(getApplicationContext(), "No se encontró la cédula digitada", Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    while (keys.hasNext()) {
                        String keyStr = (String)keys.next();

                        if (keyStr.equals("nombreCompleto")){
                            nombre.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("descripcionProvincia")) {
                            provincia.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("descripcionCanton")) {
                            canton.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("descripcionDistrito")) {
                            distrito.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("nombreCentroVotacion")) {
                            centro_votacion.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("junta")) {
                            numMesa.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("numeroElector")) {
                            numElector.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("direccionEscuela")) {
                            direccion.setText(resultadoConsulta.getString(keyStr));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
