package pac.pac.pacdron;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Consulta extends AppCompatActivity {

    Button btn_Consulta;
    TextView resultado;
    EditText inputCedula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);

        btn_Consulta = (Button);

    }
}
