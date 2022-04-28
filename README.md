[![GitHubBuild](https://github.com/bkthomps/Ajedrez/workflows/build/badge.svg)](https://github.com/bkthomps/Ajedrez)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/bkthomps/Ven/blob/master/LICENSE)

# Ajedrez
Chess game with one or two players. In either mode, only valid moves can be performed. In one-player mode, minimax
alpha-beta pruning is used to determine the computer's moves.

## Building
Java 17 is required. Use `./mvnw javafx:run` to build and run the program. You might need to `chmod +x mvnw` first.

## Moves
There are six pieces:
1. Pawn - can only move forward one square if nothing blocks it unless it has not yet been moved in which it may be
moved two squares, captures diagonally or with en-passant as described below
2. Knight - moves in an L pattern, meaning 2 squares vertically and 1 square horizontally or vice-versa, captures the
piece it lands on
3. Bishop - moves any number of squares diagonally, captures the pieces it lands on
4. Rook - moves any number of squares in a straight line, captures the pieces it lands on
5. Queen - moves any number of squares diagonally or in a straight line, captures the pieces it lands on
6. King - moves one square diagonally or in a straight line, captures the pieces it lands on

For any move, it can only be performed if the player's king does not end up in check.

Special Moves:
1. Pawn promotion - if the pawn reaches the end of the board, it must become a knight, bishop, rook, or queen on the
same turn
2. Castling - castling may be performed by moving both the king and one rook by moving the king two squares towards the
rook and moving the rook to the other side of the king if both the king and the rook have not yet been moved, there are
no pieces between the king and the rook, and the king is not in check, will not end up in check, and none of the pieces
it passes through are attacked by enemy pieces
3. En Passant - if during the last turn, the other player moved a pawn two squares, the pawn may be captures by the
player's pawn as if the pawn had only moved one square

## States
The following states are non-terminal and allow the game to continue:
1. Normal - the player may move following the normal actions
2. Check - the player must get out of check by moving any piece

The following states are terminal and the game immediately ends:
1. Checkmate (win/loss) - the king is in check and no moves can be made to get out of check
2. Stalemate (draw) - the king is not in check, but there are no legal moves which can be made
3. Threefold repetition (draw) - the board has repeated three times in the game (need not be consecutively) with the
same player to move, the same en-passant possibility, and the same castling rights
4. Fifty move rule (draw) - fifty moves have passed without a pawn advancing or a piece being captured
5. Insufficient mating material (draw) - there is not enough mating material to force a checkmate, this occurs when it
is a lone kng against a long king, king and knight, or king and bishop
