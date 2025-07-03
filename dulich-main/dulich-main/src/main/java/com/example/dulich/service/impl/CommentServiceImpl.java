package com.example.dulich.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.dto.CommentDto;
import com.example.dulich.entity.Article;
import com.example.dulich.entity.Comment;
import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.User;
import com.example.dulich.repository.ArticleRepository;
import com.example.dulich.repository.CommentRepository;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.repository.UserRepository;
import com.example.dulich.service.CommentService;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> findAllPaged(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByHotelId(Long hotelId) {
        return commentRepository.findByHotelIdAndStatus(hotelId, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByArticleId(Long articleId) {
        return commentRepository.findByArticleIdAndStatus(articleId, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByUserId(Long userId) {
        return commentRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByStatus(Integer status) {
        return commentRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findReplies(Long parentId) {
        return commentRepository.findByParentId(parentId);
    }    @Override
    @Transactional(readOnly = true)
    public Page<Comment> findWithFilters(Long hotelId, Long articleId, Long userId, Integer status, Pageable pageable) {
        return commentRepository.findWithFilters(hotelId, articleId, userId, status, pageable);
    }

    @Override
    public Comment create(CommentDto commentDto) {
        Comment comment = convertToEntity(commentDto);
        
        // Set relationship entities
        setRelationships(comment, commentDto);
        
        return commentRepository.save(comment);
    }

    @Override
    public Comment update(CommentDto commentDto, Long id) {
        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bình luận không tồn tại"));
          // Update fields
        existingComment.setContent(commentDto.getContent());
        existingComment.setName(commentDto.getName());
        existingComment.setEmail(commentDto.getEmail());
        
        if (commentDto.getStatus() != null) {
            existingComment.setStatus(commentDto.getStatus());
        }
        
        if (commentDto.getRating() != null) {
            existingComment.setRating(commentDto.getRating());
        }
        
        // Update relationships if needed
        setRelationships(existingComment, commentDto);
        
        return commentRepository.save(existingComment);
    }

    @Override
    public Comment updateStatus(Long id, Integer status) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bình luận không tồn tại"));
        
        comment.setStatus(status);
        return commentRepository.save(comment);
    }

    @Override
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto();
          dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setName(comment.getName());
        dto.setEmail(comment.getEmail());
        dto.setStatus(comment.getStatus());
        dto.setRating(comment.getRating());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
          // Set user info
        if (comment.getUser() != null) {
            dto.setUserId(comment.getUser().getId());
            dto.setUserName(comment.getUser().getUsername());
            // Assuming User entity has no avatar field, or use the appropriate method if it exists
            dto.setUserAvatar(null);        }
        
        // Set hotel info
        if (comment.getHotel() != null) {
            dto.setHotelId(comment.getHotel().getId());
            dto.setHotelName(comment.getHotel().getName());
        }
        
        // Set article info
        if (comment.getArticle() != null) {
            dto.setArticleId(comment.getArticle().getId());
            dto.setArticleTitle(comment.getArticle().getName());
        }
        
        // Set parent comment
        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
        }
        
        return dto;
    }

    @Override
    public Comment convertToEntity(CommentDto commentDto) {
        Comment comment = new Comment();
        
        if (commentDto.getId() != null) {
            comment.setId(commentDto.getId());
        }
          comment.setContent(commentDto.getContent());
        comment.setName(commentDto.getName());
        comment.setEmail(commentDto.getEmail());
        comment.setStatus(commentDto.getStatus());
        comment.setRating(commentDto.getRating());
        
        return comment;
    }
    
    // Helper method to set relationships
    private void setRelationships(Comment comment, CommentDto commentDto) {
        // Set user
        if (commentDto.getUserId() != null) {
            User user = userRepository.findById(commentDto.getUserId()).orElse(null);
            comment.setUser(user);        }
        
        // Set hotel (if specified)
        if (commentDto.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(commentDto.getHotelId()).orElse(null);
            comment.setHotel(hotel);
        }
        
        // Set article (if specified)
        if (commentDto.getArticleId() != null) {
            Article article = articleRepository.findById(commentDto.getArticleId()).orElse(null);
            comment.setArticle(article);
        }
        
        // Set parent comment (for replies)
        if (commentDto.getParentId() != null) {
            Comment parent = commentRepository.findById(commentDto.getParentId()).orElse(null);
            comment.setParent(parent);
        }    }

    @Override
    public long countByHotelId(Long hotelId) {
        return commentRepository.countByHotelId(hotelId);
    }

    @Override
    public long countByArticleId(Long articleId) {
        return commentRepository.countByArticleId(articleId);
    }

    @Override
    public long countByUserId(Long userId) {
        return commentRepository.countByUserId(userId);
    }

    @Override
    public long countByStatus(Integer status) {
        return commentRepository.countByStatus(status);
    }
}
