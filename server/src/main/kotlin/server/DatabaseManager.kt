package server

import common.model.Coordinates
import common.model.Dragon
import common.model.DragonCharacter
import common.model.DragonHead
import common.model.DragonType
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDateTime

class DatabaseManager {

    private val url = "jdbc:postgresql://pg/studs"
    private val user = "s502785"
    private val password = "zM6VXSotjlGjZMKT"

    private fun connect(): Connection = DriverManager.getConnection(url, user, password)

    fun initTables() {
        connect().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("""
                    CREATE SEQUENCE IF NOT EXISTS dragons_id_seq
                """)
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        login VARCHAR(255) PRIMARY KEY,
                        password_hash VARCHAR(64) NOT NULL
                    )
                """)
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS dragons (
                        id BIGINT PRIMARY KEY DEFAULT nextval('dragons_id_seq'),
                        name VARCHAR(255) NOT NULL,
                        coord_x REAL NOT NULL,
                        coord_y BIGINT NOT NULL,
                        creation_date TIMESTAMP NOT NULL,
                        age BIGINT NOT NULL,
                        weight DOUBLE PRECISION NOT NULL,
                        type VARCHAR(50) NOT NULL,
                        character VARCHAR(50) NOT NULL,
                        head_eyes INT,
                        head_teeth DOUBLE PRECISION,
                        owner VARCHAR(255) NOT NULL REFERENCES users(login)
                    )
                """)
            }
        }
    }

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun registerUser(login: String, passwordHash: String): Boolean {
        return try {
            connect().use { conn ->
                conn.prepareStatement("INSERT INTO users (login, password_hash) VALUES (?, ?)").use { stmt ->
                    stmt.setString(1, login)
                    stmt.setString(2, passwordHash)
                    stmt.executeUpdate()
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun authenticateUser(login: String, passwordHash: String): Boolean {
        connect().use { conn ->
            conn.prepareStatement("SELECT 1 FROM users WHERE login = ? AND password_hash = ?").use { stmt ->
                stmt.setString(1, login)
                stmt.setString(2, passwordHash)
                return stmt.executeQuery().next()
            }
        }
    }

    fun loadDragons(): Map<Long, Dragon> {
        val result = mutableMapOf<Long, Dragon>()
        connect().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT * FROM dragons")
                while (rs.next()) {
                    val id = rs.getLong("id")
                    val headEyes = rs.getObject("head_eyes")
                    val head = if (headEyes != null)
                        DragonHead(rs.getInt("head_eyes"), rs.getDouble("head_teeth"))
                    else null
                    val dragon = Dragon(
                        id = id.toInt(),
                        name = rs.getString("name"),
                        coordinates = Coordinates(rs.getFloat("coord_x"), rs.getLong("coord_y")),
                        creationDate = rs.getTimestamp("creation_date").toLocalDateTime(),
                        age = rs.getLong("age"),
                        weight = rs.getDouble("weight"),
                        type = DragonType.valueOf(rs.getString("type")),
                        character = DragonCharacter.valueOf(rs.getString("character")),
                        head = head,
                        owner = rs.getString("owner")
                    )
                    result[id] = dragon
                }
            }
        }
        return result
    }

    fun insertDragon(dragon: Dragon, owner: String): Long {
        connect().use { conn ->
            conn.prepareStatement(
                """INSERT INTO dragons (name, coord_x, coord_y, creation_date, age, weight, type, character, head_eyes, head_teeth, owner)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id"""
            ).use { stmt ->
                stmt.setString(1, dragon.name)
                stmt.setFloat(2, dragon.coordinates.x)
                stmt.setLong(3, dragon.coordinates.y)
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(dragon.creationDate))
                stmt.setLong(5, dragon.age)
                stmt.setDouble(6, dragon.weight)
                stmt.setString(7, dragon.type.name)
                stmt.setString(8, dragon.character.name)
                if (dragon.head != null) {
                    stmt.setInt(9, dragon.head!!.eyesCount)
                    stmt.setDouble(10, dragon.head!!.toothCount)
                } else {
                    stmt.setNull(9, java.sql.Types.INTEGER)
                    stmt.setNull(10, java.sql.Types.DOUBLE)
                }
                stmt.setString(11, owner)
                val rs = stmt.executeQuery()
                rs.next()
                return rs.getLong(1)
            }
        }
    }

    fun updateDragon(dragon: Dragon, owner: String): Boolean {
        connect().use { conn ->
            conn.prepareStatement(
                """UPDATE dragons SET name=?, coord_x=?, coord_y=?, creation_date=?, age=?, weight=?, type=?, character=?, head_eyes=?, head_teeth=?
                   WHERE id=? AND owner=?"""
            ).use { stmt ->
                stmt.setString(1, dragon.name)
                stmt.setFloat(2, dragon.coordinates.x)
                stmt.setLong(3, dragon.coordinates.y)
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(dragon.creationDate))
                stmt.setLong(5, dragon.age)
                stmt.setDouble(6, dragon.weight)
                stmt.setString(7, dragon.type.name)
                stmt.setString(8, dragon.character.name)
                if (dragon.head != null) {
                    stmt.setInt(9, dragon.head!!.eyesCount)
                    stmt.setDouble(10, dragon.head!!.toothCount)
                } else {
                    stmt.setNull(9, java.sql.Types.INTEGER)
                    stmt.setNull(10, java.sql.Types.DOUBLE)
                }
                stmt.setLong(11, dragon.id.toLong())
                stmt.setString(12, owner)
                return stmt.executeUpdate() > 0
            }
        }
    }

    fun deleteDragon(id: Long, owner: String): Boolean {
        connect().use { conn ->
            conn.prepareStatement("DELETE FROM dragons WHERE id=? AND owner=?").use { stmt ->
                stmt.setLong(1, id)
                stmt.setString(2, owner)
                return stmt.executeUpdate() > 0
            }
        }
    }

    fun deleteDragonsGreaterKey(key: Long, owner: String): Int {
        connect().use { conn ->
            conn.prepareStatement("DELETE FROM dragons WHERE id > ? AND owner=?").use { stmt ->
                stmt.setLong(1, key)
                stmt.setString(2, owner)
                return stmt.executeUpdate()
            }
        }
    }

    fun clearDragons(owner: String): Int {
        connect().use { conn ->
            conn.prepareStatement("DELETE FROM dragons WHERE owner=?").use { stmt ->
                stmt.setString(1, owner)
                return stmt.executeUpdate()
            }
        }
    }
}