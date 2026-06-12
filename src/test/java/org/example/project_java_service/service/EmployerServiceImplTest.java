package org.example.project_java_service.service;

import org.example.project_java_service.model.dto.request.JobPostingRequest;
import org.example.project_java_service.model.dto.response.ApplicationResponse;
import org.example.project_java_service.model.entity.JobApplication;
import org.example.project_java_service.model.entity.JobPosting;
import org.example.project_java_service.model.entity.User;
import org.example.project_java_service.repository.JobApplicationRepository;
import org.example.project_java_service.repository.JobPostingRepository;
import org.example.project_java_service.repository.UserRepository;
import org.example.project_java_service.service.impl.EmployerServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class EmployerServiceImplTest {

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @InjectMocks
    private EmployerServiceImpl employerService;

    // Test 1: Tạo tin tuyển dụng thành công
    @Test
    void testCreateJob_Success() {
        JobPostingRequest request = new JobPostingRequest();
        User employer = User.builder().id(1L).username("employer_test").build();

        Mockito.when(userRepository.findByUsername("employer_test")).thenReturn(Optional.of(employer));

        String response = employerService.createJob(request, "employer_test");

        Assertions.assertEquals("Đăng tin tuyển dụng thành công. Tin của bạn đang chờ Admin phê duyệt!", response);
        Mockito.verify(jobPostingRepository, Mockito.times(1)).save(Mockito.any(JobPosting.class));
    }

    // Test 2: Xóa tin tuyển dụng thành công
    @Test
    void testDeleteJob_Success() {
        User employer = User.builder().id(1L).username("employer_test").build();
        JobPosting job = JobPosting.builder().id(100L).employer(employer).build();

        Mockito.when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(job));

        String response = employerService.deleteJob(100L, "employer_test");

        Assertions.assertEquals("Đã xóa tin tuyển dụng thành công!", response);
        Mockito.verify(jobPostingRepository, Mockito.times(1)).delete(job);
    }

    // Test 3: Xóa tin tuyển dụng thất bại do không phải chủ bài đăng
    @Test
    void testDeleteJob_Fail_NotOwner() {
        User trueOwner = User.builder().id(1L).username("employer_test").build();
        JobPosting job = JobPosting.builder().id(100L).employer(trueOwner).build();

        Mockito.when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(job));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> employerService.deleteJob(100L, "hacker_user"));

        Assertions.assertEquals("Bạn không có quyền xóa tin tuyển dụng này!", exception.getMessage());
    }

    // Test 4: Cập nhật trạng thái tin tuyển dụng (Update Job) thành công
    @Test
    void testUpdateJob_Success() {
        JobPostingRequest request = new JobPostingRequest();
        User employer = User.builder().id(1L).username("employer_test").build();
        JobPosting job = JobPosting.builder().id(100L).employer(employer).build();

        Mockito.when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(job));

        String response = employerService.updateJob(100L, request, "employer_test");

        Assertions.assertEquals("Cập nhật thành công. Tin đang được đưa về trạng thái chờ duyệt lại!", response);
        Mockito.verify(jobPostingRepository, Mockito.times(1)).save(job);
    }

    // Test 5: Xem danh sách ứng viên nộp vào công việc của mình thành công
    @Test
    void testGetApplicationsForJob_Success() {
        User employer = User.builder().id(1L).username("employer_test").build();
        JobPosting job = JobPosting.builder().id(100L).employer(employer).build();

        User candidate = User.builder().username("candidate1").email("can@gmail.com").build();
        JobApplication application = JobApplication.builder().id(10L).jobPosting(job).candidate(candidate).build();

        Mockito.when(jobPostingRepository.findById(100L)).thenReturn(Optional.of(job));
        Mockito.when(jobApplicationRepository.findByJobPostingId(100L)).thenReturn(List.of(application));

        List<ApplicationResponse> responses = employerService.getApplicationsForJob(100L, "employer_test");

        Assertions.assertEquals(1, responses.size());
        Assertions.assertEquals("candidate1", responses.get(0).getCandidateUsername());
    }
}