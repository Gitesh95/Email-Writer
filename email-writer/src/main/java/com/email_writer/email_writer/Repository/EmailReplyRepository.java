package com.email_writer.email_writer.Repository;

import com.email_writer.email_writer.Model.EmailReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailReplyRepository extends JpaRepository<EmailReply,Long> {
    Optional<EmailReply> findTopByOrderByIdDesc();  // get latest reply

}
