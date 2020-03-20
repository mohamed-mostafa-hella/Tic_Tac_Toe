package com.example.tictactoe.UI;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tictactoe.Pojo.Move;
import com.example.tictactoe.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MainActivity extends Activity implements View.OnClickListener {

    @BindView(R.id.player1)
    TextView player1;
    @BindView(R.id.player2)
    TextView player2;
    @BindView(R.id.player_turn)
    TextView playerTurn;
    @BindView(R.id.reset)
    Button reset;

    private Button onePlayerButton , twoPlayerButton;

    private Button[][] buttons = new Button[3][3];
    private BottomSheetDialog bottomSheetDialog;

    private boolean player1turn = true;

    private int roundCount = 0;

    private int player1Points = 0;
    private int player2Points = 0;

    private boolean isOnePlayer=true;

    private String[][] field = new String[3][3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //hide action bar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String buttonID = "button_" + i + j;
                int id = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[i][j] = findViewById(id);
                buttons[i][j].setOnClickListener(this);
            }
        }

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reset
                resetBord();
                showDialog();
            }
        });
        getData();

    }

    void showDialog(){
        bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        bottomSheetDialog.setContentView(R.layout.dialog);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.setCancelable(false);
        onePlayerButton = bottomSheetDialog.findViewById(R.id.one_player);
        twoPlayerButton = bottomSheetDialog.findViewById(R.id.two_player);
        onePlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOnePlayer = true;
                bottomSheetDialog.dismiss();
            }
        });

        twoPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOnePlayer = false;
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }

    @Override
    public void onClick(View v) {
        if (!((Button) v).getText().toString().equals("")) {
            return;
        }
        if (player1turn) {
            ((Button) v).setText("X");
            playerTurn.setText("O");
        } else if (!isOnePlayer){
            ((Button) v).setText("O");
            playerTurn.setText("X");
        }
        bostPlay();
    }

    void bostPlay() {
        roundCount++;
        getData();
        int eval = evaluation();
        if (eval == -10) {
            player1Wins();
        } else if (eval == 10) {
            player2Wins();
        } else if (roundCount == 9) {
            draw();
        } else {
            player1turn = !player1turn;
        }

        if (!player1turn && isOnePlayer) {
            AIPlay();
        }
    }


    /////////// soft max ///////////////////////////////
    void AIPlay() {
        Move move = getPestMove();
        int i = move.getRow();
        int j = move.getCol();
        if (i > -1 && j > -1) {
            buttons[i][j].setText("O");
            playerTurn.setText("X");
            Log.d("pest move", "" + i + "  " + j + "\n");
            bostPlay();
        } else {
            Toast.makeText(this, "there is no best move", Toast.LENGTH_SHORT).show();
        }
    }

    private Move getPestMove() {
        Move bestMove = new Move(-1, -1);
        int boint = -1000;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (field[i][j].equals("")) {
                    field[i][j] = "O";
                    int score = minimax(0, false);
                    field[i][j] = "";
                    if (score > boint) {
                        bestMove.setRow(i);
                        bestMove.setCol(j);
                        boint = score;
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(int depth, boolean isMax) {

        int score = evaluation();
        if (score == 10 || score == -10) return score;

        if (!isMovesLeft()) return 0;

        if (isMax) {
            int best = -1000;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (field[i][j].equals("")) {
                        field[i][j] = "O";
                        best = max(best, minimax(depth + 1, !isMax));
                        field[i][j] = "";
                    }
                }
            }
            return best;
        } else {

            int best = 1000;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (field[i][j].equals("")) {
                        field[i][j] = "X";
                        best = min(best, minimax(depth + 1, !isMax));
                        field[i][j] = "";
                    }
                }
            }
            return best;
        }
    }

    int evaluation() {
        for (int i = 0; i < 3; i++) {
            // Rows
            if (equal3(field[i][0], field[i][1], field[i][2])) {
                return calculate(field[i][0]);
            }
            // Columns
            if (equal3(field[0][i], field[1][i], field[2][i])) {
                return calculate(field[0][i]);
            }
        }

        if (equal3(field[0][0], field[1][1], field[2][2])) {
            return calculate(field[0][0]);
        } else if (equal3(field[0][2], field[1][1], field[2][0])) {
            return calculate(field[0][2]);
        } else {
            return 0;
        }

    }

    int calculate(String a) {
        if (a.equals("X")) return -10;
        else return 10;
    }

    boolean isMovesLeft() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (field[i][j].equals(""))
                    return true;
        return false;
    }


    ////////////////////////////////////////////////////

    public void draw(View view) {
        draw();
    }

    //the result functions

    private void draw() {
        Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();
        cleanBord();
    }

    private void player2Wins() {
        player2Points++;
        Toast.makeText(this, "player O wins", Toast.LENGTH_SHORT).show();
        updatePointstText();
        cleanBord();

    }

    private void player1Wins() {
        player1Points++;
        Toast.makeText(this, "player X wins", Toast.LENGTH_SHORT).show();
        updatePointstText();
        cleanBord();

    }

    private void updatePointstText() {
        player1.setText("Player X: " + player1Points);
        player2.setText("Player O: " + player2Points);
    }

    private void cleanBord() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
            }
        }
        roundCount = 0;
        player1turn = true;
        playerTurn.setText("X");

    }

    //check if there is a winner

    private void getData() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }
    }

    // reset the game

    private void resetBord() {
        cleanBord();
        player1Points = 0;
        player2Points = 0;
        updatePointstText();
    }

    //
    boolean equal3(String a, String b, String c) {
        return (a.equals(b) && b.equals(c) && !a.equals(""));
    }

    //save instance

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("roundCount", roundCount);
        outState.putInt("player1Points", player1Points);
        outState.putInt("player2Points", player2Points);
        outState.putBoolean("player1Turn", player1turn);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Toast.makeText(this, "restore", Toast.LENGTH_SHORT).show();
        roundCount = savedInstanceState.getInt("roundCount");
        player1Points = savedInstanceState.getInt("player1Points");
        player2Points = savedInstanceState.getInt("player2Points");
        player1turn = savedInstanceState.getBoolean("player1Turn");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (roundCount==0 && player1Points == 0 && player2Points == 0)
            showDialog();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (bottomSheetDialog !=null && bottomSheetDialog.isShowing()){
            bottomSheetDialog.dismiss();
        }
    }

}
