package com.srf.dao;

import com.srf.models.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagDAO {
    private Connection connection;

    public TagDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Tag> getAllTags() throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT * FROM tags";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tags.add(new Tag(
                        resultSet.getInt("userId"),
                        resultSet.getInt("movieId"),
                        resultSet.getString("tag")
                ));
            }
        }
        return tags;
    }

    public void addTag(Tag tag) throws SQLException {
        String sql = "INSERT INTO tags (userId, movieId, tag) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, tag.getUserId());
            statement.setInt(2, tag.getMovieId());
            statement.setString(3, tag.getTag());
            statement.executeUpdate();
        }
    }
}
