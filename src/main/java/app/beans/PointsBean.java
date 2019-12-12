package app.beans;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

@ManagedBean(name = "points")
@SessionScoped
public class PointsBean implements Serializable {
    private Deque<PointBean> data;
    private int size;

    private double x;
    private double r;
    private BigDecimal y;
    private String result;
    private String unique;

    @Resource(name = "datasources/oracle")
    private DataSource ds;

    {
        data = new ArrayDeque<>();
        try {
            Context context = new InitialContext();
            ds = (DataSource) context.lookup("java:jboss/datasources/oracle");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void addPoint(PointBean point) throws SQLException {
        if (ds == null) throw new SQLException("No data source");
        Connection conn = ds.getConnection();
        if (conn == null) throw new SQLException("No connection");

        try {
            conn.setAutoCommit(false);
            boolean committed = false;
            try {
                PreparedStatement newpoint = conn.prepareStatement("INSERT INTO points VALUES (?,?,?,?,?)");
                newpoint.setString(1, point.getUnique());
                newpoint.setDouble(2, point.getX());
                newpoint.setBigDecimal(3, point.getY());
                newpoint.setDouble(4, point.getR());
                newpoint.setString(5, point.getResult());
                newpoint.executeUpdate();
                conn.commit();
                committed = true;
            } finally {
                if (!committed) conn.rollback();
            }
        } finally {
            conn.close();
            data.add(point);
        }
    }

    @Deprecated
    public void updatePoints() throws SQLException {
        data.clear();
        if (ds == null) throw new SQLException("No data source");
        Connection conn = ds.getConnection();
        if (conn == null) throw new SQLException("No connection");
        try {
            Statement usersPoints = conn.createStatement();
            ResultSet result = usersPoints.executeQuery("SELECT id, x, y, r, result  from points");
            while (result.next()) {
                data.add(new PointBean(
                        result.getDouble("x"),
                        result.getBigDecimal("y"),
                        result.getDouble("r"),
                        result.getString("result"),
                        result.getString("id")
                ));
            }
        } finally {
            conn.close();
        }
    }


    public Deque<PointBean> getData() {
        return data;
    }

    public void setData(Deque<PointBean> points) {
        this.data = points;
    }

    public String JSON() {
        return '[' + data.parallelStream()
                .map(point -> String.format("{\"x\": %s, \"y\": %s, \"r\": %s, \"result\": %s}",
                        point.getX(), point.getY(), point.getR(), point.getResult()))
                .collect(Collectors.joining(", ")) + ']';
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public BigDecimal getY() {
        return y;
    }

    public void setY(BigDecimal y) {
        this.y = y;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }
}
